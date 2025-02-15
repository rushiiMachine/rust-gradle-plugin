use jni::objects::{JObject, JString};
use jni::sys::{jint, jobject, JNI_VERSION_1_6};
use jni::{JNIEnv, JavaVM};
use jni_fn::jni_fn;
use log::info;
use std::borrow::Cow;
use std::ffi::c_void;
use std::ops::Deref;

#[no_mangle]
pub extern "system" fn JNI_OnLoad(_vm: JavaVM, _reserved: c_void) -> jint {
    android_log::init("libhello").unwrap();

    JNI_VERSION_1_6
}

#[jni_fn("dev.rushii.rgp.LibHello")]
pub fn helloWorld(mut env: JNIEnv, _class: JObject, name: JString) -> jobject {
    info!("Invoked native method LibHello.helloWorld");

    let jni_name = env.get_string(&name).unwrap();
    let name: Cow<str> = jni_name.deref().into();

    let string = env.new_string(&*format!("Hello {name}!")).unwrap();

    string.into_raw()
}
