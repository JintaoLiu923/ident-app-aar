
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
typedef   signed          char int8_t;
typedef   signed short     int int16_t;
typedef   signed           int int32_t;
typedef unsigned          char uint8_t;
typedef unsigned short     int uint16_t;
typedef unsigned           int uint32_t;

//д���ݼ��ܺ�����TBC
int8_t* writeEncode(int8_t singlePageData[], int32_t singleLen, int8_t writeKey);
//�����ݽ��ܺ�����TBC
int8_t* readDecode(int8_t* singlePageData, int32_t singleLen, int8_t readKey);
//�Ӳ�ֵ���㺯��
int8_t* EnInterpolation(int8_t itspv, int8_t Data[], int32_t dataLen, int8_t RAND[], int32_t randLen);
//ȥ��ֵ���㺯��
struct mDataint8_t* DeInterpolation(int8_t ITSP, int8_t Data[], int32_t dataLen);

//�ֽ�ת2����
struct mDataInt* byteToBinary(int8_t HEX[], int32_t hexLen);
//2��������תbyte����
struct mDataint8_t* binaryToByte(int32_t BIN[], int32_t binLen);
//�ַ�ת�ֽ�
struct mDataint8_t* stringToBytes(int8_t pageStrData[], int32_t length);
//��ת����˳�� int
int32_t* reverseArrayforInt(int32_t inputArray[], int32_t len);
//��ת����˳�� int8_t
int8_t* reverseArrayforint8_t(int8_t* inputArray, int32_t  Arrlen);

struct mDataInt {
    int32_t* data;
    int32_t len;
};

struct mDataint8_t {
    int8_t* data;
    int32_t len;
};
