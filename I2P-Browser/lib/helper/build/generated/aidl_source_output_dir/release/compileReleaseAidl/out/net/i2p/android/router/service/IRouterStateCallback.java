/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package net.i2p.android.router.service;
/**
 * Callback interface used to send synchronous notifications of the current
 * RouterService state back to registered clients. Note that this is a
 * one-way interface so the server does not block waiting for the client.
 */
public interface IRouterStateCallback extends android.os.IInterface
{
  /** Default implementation for IRouterStateCallback. */
  public static class Default implements net.i2p.android.router.service.IRouterStateCallback
  {
    /**
         * Called when the state of the I2P router changes.
         *
         * @param newState may be null if the State is not known. See
         * {@link IRouterState#getState()}.
         */
    @Override public void stateChanged(net.i2p.android.router.service.State newState) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements net.i2p.android.router.service.IRouterStateCallback
  {
    private static final java.lang.String DESCRIPTOR = "net.i2p.android.router.service.IRouterStateCallback";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an net.i2p.android.router.service.IRouterStateCallback interface,
     * generating a proxy if needed.
     */
    public static net.i2p.android.router.service.IRouterStateCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof net.i2p.android.router.service.IRouterStateCallback))) {
        return ((net.i2p.android.router.service.IRouterStateCallback)iin);
      }
      return new net.i2p.android.router.service.IRouterStateCallback.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_stateChanged:
        {
          data.enforceInterface(descriptor);
          net.i2p.android.router.service.State _arg0;
          if ((0!=data.readInt())) {
            _arg0 = net.i2p.android.router.service.State.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          this.stateChanged(_arg0);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements net.i2p.android.router.service.IRouterStateCallback
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
           * Called when the state of the I2P router changes.
           *
           * @param newState may be null if the State is not known. See
           * {@link IRouterState#getState()}.
           */
      @Override public void stateChanged(net.i2p.android.router.service.State newState) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((newState!=null)) {
            _data.writeInt(1);
            newState.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_stateChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().stateChanged(newState);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static net.i2p.android.router.service.IRouterStateCallback sDefaultImpl;
    }
    static final int TRANSACTION_stateChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    public static boolean setDefaultImpl(net.i2p.android.router.service.IRouterStateCallback impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static net.i2p.android.router.service.IRouterStateCallback getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  /**
       * Called when the state of the I2P router changes.
       *
       * @param newState may be null if the State is not known. See
       * {@link IRouterState#getState()}.
       */
  public void stateChanged(net.i2p.android.router.service.State newState) throws android.os.RemoteException;
}
