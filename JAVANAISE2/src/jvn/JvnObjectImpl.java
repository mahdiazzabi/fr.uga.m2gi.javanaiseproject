package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

	private Serializable object = null;
	transient private LockState lock = LockState.NL;

	private enum LockState {
		NL, // no local lock
		RLC, // read lock cached
		WLC, // write lock cached
		RLT, // read lock taken
		WLT, // write lock taken
		RLTWLC; // read lock taken & write lock cached
	}

	public JvnObjectImpl(Serializable object) {
		this.object = object;
		this.setLock(LockState.NL);
	}

	public void jvnLockRead() throws JvnException {
		synchronized (this.object) {
			System.out.print("Operation LockRead : initial state : "+ this.lock + " " );
			
			if (this.lock == LockState.NL) {
				// we will ask the coordinateur to take lock R and then this.setLock(LockState.RLT);
				this.setLock(LockState.RLT);
			} else if(this.lock == LockState.WLC) {
				this.setLock(LockState.RLTWLC);
			} else if(this.lock == LockState.RLC)
				this.setLock(LockState.RLT);
			//nous considérons que si l'objet est vérrouillé en écriture WLT
			//nous gardons le verrous WLT car > RLT

			System.out.println("passed to : "+ this.lock );
		}
	}

	public void jvnLockWrite() throws JvnException {
		synchronized (this.object) {

			System.out.print("Operation LockWrite : initial state : "+ this.lock +" ");
			if (this.lock == LockState.NL) {
				// we will ask the coordinateur to take lock W and then this.setLock(LockState.WLT);
				this.setLock(LockState.WLT);
			} else {
				this.setLock(LockState.WLT);
			}
			System.out.println("passed to : "+ this.lock );
		}

	}

	public void jvnUnLock() throws JvnException {
		synchronized (this.object) {

			System.out.print("Unlock operation : initial lock :" + this.lock + " ");
			switch (this.lock) {
			case NL:
				break;
			case RLC:
				this.setLock(LockState.NL);
				break;
			case WLC:
				this.setLock(LockState.NL);
				break;
			case RLT:
				this.setLock(LockState.RLC);
				break;
			case WLT:
				this.setLock(LockState.WLC);
				break;
			case RLTWLC:
				this.setLock(LockState.WLC);
				break;
			default:
				break;
			}
		}

		System.out.println(" passed to :" + this.lock);
	}

	public int jvnGetObjectId() throws JvnException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		// TODO Auto-generated method stub
		return this.object;
	}

	public void jvnInvalidateReader() throws JvnException {
		// TODO Auto-generated method stub

	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		synchronized (this.object) {
			while (this.lock == LockState.WLT) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			jvnUnLock();
			return this.object;
		}
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		synchronized (this.object) {
			while (this.lock == LockState.WLT) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return this.object;
		}
	}

	public LockState getLock() {
		return this.lock;
	}

	public JvnObjectImpl setLock(LockState lock) {
		this.lock = lock;
		return this;
	}
}
