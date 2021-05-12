#include<jni.h>
#include <malloc.h>
JNIEXPORT jbyteArray JNICALL
Java_com_ident_validatorr_core_utils_Test_1lib_EnInterpolation(JNIEnv *env, jclass type,jbyte rk,jbyte wk,jbyte itsp,jbyte intsel,
                                                               jbyte itspv, jbyteArray Data_,
                                                               jint dataLen, jbyteArray RAND_,
                                                               jint randLen) {
    jbyte *Data = (*env)->GetByteArrayElements(env, Data_, NULL);
    jbyte *RAND = (*env)->GetByteArrayElements(env, RAND_, NULL);
    char *result= EnInterpolation(itspv, Data_, dataLen, RAND_, randLen)
    int len=strlen(result);
    jbyteArray newByteArray = (*env)->NewByteArray(env,len);
    (*env)->SetByteArrayRegion(env,newByteArray, 0, len,result);
    return newByteArray;
}

JNIEXPORT jbyteArray JNICALL
Java_com_ident_validatorr_core_utils_Test_1lib_DeInterpolation(JNIEnv *env, jclass type, jbyte rk,jbyte wk,jbyte itsp,jbyte intsel,
                                                               jbyteArray Data_, jint dataLen) {
    jbyte *Data = (*env)->GetByteArrayElements(env, Data_, NULL);
    char *result=DeInterpolation(itsp, Data_, dataLen)
    int len=strlen(result);
    jbyteArray newByteArray = (*env)->NewByteArray(env,len);
    (*env)->SetByteArrayRegion(env,newByteArray, 0, len,result);
    return newByteArray;
}