# btserial
Code for event-injection based game controller on android

1. Create an Android Studio project with this as the source code and build it
2. Decompile the built application and the related application to add controller functionality using apktool
3. Delete all files in directory /smali/com/retorikal/ whose name does not contain "BTService"
4. Copy /smali/com/retorikal/ to the root directory of the decompiled target application
5. Add the following lines to AndroidManifet.xml on the appropriate places:
    Bluetooth permission
        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
        <uses-permission android:name="android.permission.BLUETOOTH"/>
    Register BTService
        <service android:exported="false"
          android:name="com.retorikal.btserial.BTService"
          android:stopWithTask="true"/>          
6. Analyze the Main Activity call chain int the smali files and locate 
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V
7. Add below it:
    invoke-static {p0}, Lcom/retorikal/btserial/BTService;->start(Landroid/content/Context;)V
8. Recompile the target application files
