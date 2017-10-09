package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

	int joi;
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

	public JvnObjectImpl(Serializable object, int joi) {
		this.object = object;
		this.setLock(LockState.WLT);
		this.joi = joi;
	}

	public synchronized void jvnLockRead() throws JvnException {
		if (this.lock == null) {
			this.lock = LockState.NL;
		}

		System.out.println("Operation LockRead : initial state : " + this.lock + " ");

		if (this.lock == LockState.NL) {
			object = JvnServerImpl.jvnGetServer().jvnLockRead(this.joi);
			this.setLock(LockState.RLT);
		} else if (this.lock == LockState.WLC) {
			this.setLock(LockState.RLTWLC);
		} else if (this.lock == LockState.RLC) {
			this.setLock(LockState.RLT);
		}
		// nous considérons que si l'objet est vérrouillé en écriture WLT
		// nous gardons le verrous WLT car > RLT

		System.out.println("passed to : " + this.lock);
	}

	public synchronized void jvnLockWrite() throws JvnException {
		if (this.lock == null) {
			this.lock = LockState.NL;
		}

		System.out.println("Operation LockWrite : initial state : " + this.lock + " Object : " + joi);

		if (this.lock == LockState.NL) {
			object = JvnServerImpl.jvnGetServer().jvnLockWrite(this.joi);
			this.setLock(LockState.WLT);
		} else {
			this.setLock(LockState.WLT);
		}

		System.out.println("passed to : " + this.lock);
	}

	public synchronized void jvnUnLock() throws JvnException {
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
				throw new JvnException("jvnUnlock Error");
		}

		notify();

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

	public synchronized void jvnInvalidateReader() throws JvnException {
		while (this.lock == LockState.RLT) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		jvnUnLock();
	}

	public synchronized Serializable jvnInvalidateWriter() throws JvnException {
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

	public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		while (this.lock == LockState.WLT) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return this.object;
	}

	public LockState getLock() {
		return this.lock;
	}

	public JvnObjectImpl setLock(LockState lock) {
		this.lock = lock;

		return this;
	}
}
