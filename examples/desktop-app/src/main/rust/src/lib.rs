use jni::objects::{JObject, JString};
use jni::sys::jobject;
use jni::JNIEnv;
use jni_fn::jni_fn;
use std::borrow::Cow;
use std::ops::Deref;

#[jni_fn("dev.rushii.libhello.LibHello")]
pub fn helloWorld(mut env: JNIEnv, _class: JObject, name: JString) -> jobject {
    let jni_name = env.get_string(&name).unwrap();
    let name: Cow<str> = jni_name.deref().into();

    let string = env.new_string(&*format!("Hello {name}!")).unwrap();

    string.into_raw()
}
