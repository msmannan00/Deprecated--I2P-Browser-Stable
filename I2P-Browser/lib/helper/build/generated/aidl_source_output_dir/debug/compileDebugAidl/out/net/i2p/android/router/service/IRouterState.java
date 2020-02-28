/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package net.i2p.android.router.service;
/**
 * An interface for determining the state of the I2P RouterService.
 */
public interface IRouterState extends android.os.IInterface
{
  /** Default implementation for IRouterState. */
  public static class Default implements net.i2p.android.router.service.IRouterState
  {
    /**
         * This allows I2P to inform on state changes.
         */
    @Override public void registerCallback(net.i2p.android.router.service.IRouterStateCallback cb) throws android.os.RemoteException
    {
    }
    /**
         * Remove registered callback interface.
         */
    @Override public void unregisterCallback(net.i2p.android.router.service.IRouterStateCallback cb) throws android.os.RemoteException
    {
    }
    /**
         * Determines whether the RouterService has been started. If it hasn't, no
         * state changes will ever occur from this RouterService instance, and the
         * client should unbind and inform the user that the I2P router is not
         * running (and optionally send a net.i2p.android.router.START_I2P Intent).
         */
    @Override public boolean isStarted() throws android.os.RemoteException
    {
      return false;
    }
    /**
         * Get the state of the I2P router.
         *
         * @return null if the State is not known, e.g. a new state has been added
         * to State.aidl in I2P Android. Client app devs should update their client
         * library, or their copy of State.aidl, if they are getting null States.
         * Future State.aidl versions will be backwards-compatible.
         */
    @Override public net.i2p.android.router.service.State getState() throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements net.i2p.android.router.service.IRouterState
  {
    private static final java.lang.String DESCRIPTOR = "net.i2p.android.router.service.IRouterState";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an net.i2p.android.router.service.IRouterState interface,
     * generating a proxy if needed.
     */
    public static net.i2p.android.router.service.IRouterState asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof net.i2p.android.router.service.IRouterState))) {
        return ((net.i2p.android.router.service.IRouterState)iin);
      }
      return new net.i2p.android.router.service.IRouterState.Stub.Proxy(obj);
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
        case TRANSACTION_registerCallback:
        {
          data.enforceInterface(descriptor);
          net.i2p.android.router.service.IRouterStateCallback _arg0;
          _arg0 = net.i2p.android.router.service.IRouterStateCallback.Stub.asInterface(data.readStrongBinder());
          this.registerCallback(_arg0);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_unregisterCallback:
        {
          data.enforceInterface(descriptor);
          net.i2p.android.router.service.IRouterStateCallback _arg0;
          _arg0 = net.i2p.android.router.service.IRouterStateCallback.Stub.asInterface(data.readStrongBinder());
          this.unregisterCallback(_arg0);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_isStarted:
        {
          data.enforceInterface(descriptor);
          boolean _result = this.isStarted();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          return true;
        }
        case TRANSACTION_getState:
        {
          data.enforceInterface(descriptor);
          net.i2p.android.router.service.State _result = this.getState();
          reply.writeNoException();
          if ((_result!=null)) {
            reply.writeInt(1);
            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          }
          else {
            reply.writeInt(0);
          }
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements net.i2p.android.router.service.IRouterState
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
           * This allows I2P to inform on state changes.
           */
      @Override public void registerCallback(net.i2p.android.router.service.IRouterStateCallback cb) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().registerCallback(cb);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
           * Remove registered callback interface.
           */
      @Override public void unregisterCallback(net.i2p.android.router.service.IRouterStateCallback cb) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().unregisterCallback(cb);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
           * Determines whether the RouterService has been started. If it hasn't, no
           * state changes will ever occur from this RouterService instance, and the
           * client should unbind and inform the user that the I2P router is not
           * running (and optionally send a net.i2p.android.router.START_I2P Intent).
           */
      @Override public boolean isStarted() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isStarted, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().isStarted();
          }
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
           * Get the state of the I2P router.
           *
           * @return null if the State is not known, e.g. a new state has been added
           * to State.aidl in I2P Android. Client app devs should update their client
           * library, or their copy of State.aidl, if they are getting null States.
           * Future State.aidl versions will be backwards-compatible.
           */
      @Override public net.i2p.android.router.service.State getState() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        net.i2p.android.router.service.State _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getState, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getState();
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = net.i2p.android.router.service.State.CREATOR.createFromParcel(_reply);
          }
          else {
            _result = null;
          }
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public static net.i2p.android.router.service.IRouterState sDefaultImpl;
    }
    static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_isStarted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_getState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    public static boolean setDefaultImpl(net.i2p.android.router.service.IRouterState impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static net.i2p.android.router.service.IRouterState getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  /**
       * This allows I2P to inform on state changes.
       */
  public void registerCallback(net.i2p.android.router.service.IRouterStateCallback cb) throws android.os.RemoteException;
  /**
       * Remove registered callback interface.
       */
  public void unregisterCallback(net.i2p.android.router.service.IRouterStateCallback cb) throws android.os.RemoteException;
  /**
       * Determines whether the RouterService has been started. If it hasn't, no
       * state changes will ever occur from this RouterService instance, and the
       * client should unbind and inform the user that the I2P router is not
       * running (and optionally send a net.i2p.android.router.START_I2P Intent).
       */
  public boolean isStarted() throws android.os.RemoteException;
  /**
       * Get the state of the I2P router.
       *
       * @return null if the State is not known, e.g. a new state has been added
       * to State.aidl in I2P Android. Client app devs should update their client
       * library, or their copy of State.aidl, if they are getting null States.
       * Future State.aidl versions will be backwards-compatible.
       */
  public net.i2p.android.router.service.State getState() throws android.os.RemoteException;
}
