/*___Generated_by_IDEA___*/

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\SeniorProject\\InfoCam\\infocam\\plugins\\infocam-library\\src\\org\\infocam\\lib\\service\\IDataHandlerService.aidl
 */
package com.infocam.lib.service;
/**
 * Android Interface Definition Language for contact between services in different threads,
 * In this case: The IDataHandlerService connects the infocam core with the datahandlers of the plugins.
 */
public interface IDataHandlerService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.infocam.lib.service.IDataHandlerService
{
private static final java.lang.String DESCRIPTOR = "com.infocam.lib.service.IDataHandlerService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.infocam.lib.service.IDataHandlerService interface,
 * generating a proxy if needed.
 */
public static com.infocam.lib.service.IDataHandlerService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.infocam.lib.service.IDataHandlerService))) {
return ((com.infocam.lib.service.IDataHandlerService)iin);
}
return new com.infocam.lib.service.IDataHandlerService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getPid:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getPid();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_build:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.build();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getPluginName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getPluginName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getUrlMatch:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String[] _result = this.getUrlMatch(_arg0);
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_getDataMatch:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String[] _result = this.getDataMatch(_arg0);
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_load:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
java.util.List<com.infocam.lib.marker.InitialMarkerData> _result = this.load(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
public static class Proxy implements com.infocam.lib.service.IDataHandlerService
{
private android.os.IBinder mRemote;
public Proxy(android.os.IBinder remote)
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
/** Request the process ID of this service. */
@Override public int getPid() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPid, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String build() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_build, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getPluginName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPluginName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String[] getUrlMatch(java.lang.String processorName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(processorName);
mRemote.transact(Stub.TRANSACTION_getUrlMatch, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String[] getDataMatch(java.lang.String processorName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(processorName);
mRemote.transact(Stub.TRANSACTION_getDataMatch, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.List<com.infocam.lib.marker.InitialMarkerData> load(java.lang.String processorName, java.lang.String rawData, int taskId, int colour) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<com.infocam.lib.marker.InitialMarkerData> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(processorName);
_data.writeString(rawData);
_data.writeInt(taskId);
_data.writeInt(colour);
mRemote.transact(Stub.TRANSACTION_load, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(com.infocam.lib.marker.InitialMarkerData.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
public static final int TRANSACTION_getPid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
public static final int TRANSACTION_build = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
public static final int TRANSACTION_getPluginName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
public static final int TRANSACTION_getUrlMatch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
public static final int TRANSACTION_getDataMatch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
public static final int TRANSACTION_load = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
}
/** Request the process ID of this service. */
public int getPid() throws android.os.RemoteException;
public java.lang.String build() throws android.os.RemoteException;
public java.lang.String getPluginName() throws android.os.RemoteException;
public java.lang.String[] getUrlMatch(java.lang.String processorName) throws android.os.RemoteException;
public java.lang.String[] getDataMatch(java.lang.String processorName) throws android.os.RemoteException;
public java.util.List<com.infocam.lib.marker.InitialMarkerData> load(java.lang.String processorName, java.lang.String rawData, int taskId, int colour) throws android.os.RemoteException;
}
