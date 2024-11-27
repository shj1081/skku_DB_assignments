package simpledb.tx.concurrency;

import java.util.HashMap;
import java.util.Map;

import simpledb.file.BlockId;

/**
 * The concurrency manager for the transaction.
 * Each transaction has its own concurrency manager.
 * The concurrency manager keeps track of which locks the
 * transaction currently has, and interacts with the
 * global lock table as needed.
 * 
 * @author Edward Sciore
 */
public class ConcurrencyMgr {

    /**
     * The global lock table. This variable is static because
     * all transactions share the same table.
     */
    private static LockTable locktbl = new LockTable();
    private Map<BlockId, String> locks = new HashMap<BlockId, String>();
    private int txnum; // Add transaction number field

    public ConcurrencyMgr(int txnum) { // Modified constructor
        this.txnum = txnum;
        locks = new HashMap<>();
    }

    /**
     * Obtain an SLock on the block, if necessary.
     * The method will ask the lock table for an SLock
     * if the transaction currently has no locks on that block.
     * 
     * @param blk a reference to the disk block
     */
    public void sLock(BlockId blk) {
        if (locks.get(blk) == null) {
            locktbl.sLock(blk, txnum); // Pass txnum
            locks.put(blk, "S");
        }
    }

    /**
     * Obtain an XLock on the block, if necessary.
     * If the transaction does not have an XLock on that block,
     * then the method first gets an SLock on that block
     * (if necessary), and then upgrades it to an XLock.
     * 
     * @param blk a reference to the disk block
     */
    public void xLock(BlockId blk) {
        if (!hasXLock(blk)) {
            sLock(blk);
            locktbl.xLock(blk, txnum); // Pass txnum
            locks.put(blk, "X");
        }
    }

    /**
     * Release all locks by asking the lock table to
     * unlock each one.
     */
    public void release() {
        for (BlockId blk : locks.keySet())
            locktbl.unlock(blk, txnum); // Pass txnum
        locks.clear();
    }

    private boolean hasXLock(BlockId blk) {
        String locktype = locks.get(blk);
        return locktype != null && locktype.equals("X");
    }
}