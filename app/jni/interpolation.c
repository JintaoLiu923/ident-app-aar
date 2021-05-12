#include "ras_util_encode.h"
#include <stdio.h>

//д���ݼ��ܺ�����TBC
int8_t* writeEncode(int8_t singlePageData[], int32_t singleLen, int8_t writeKey)
{
    int8_t* dataEncoded = (int8_t*)malloc(sizeof(int8_t) * singleLen);
	 
    //�ж�ҳ�����Ƿ���Ч
    if ((singleLen != 4) && (singleLen != 2))
    {
				free(dataEncoded);
        return NULL;
    }
		
		int32_t dataEncodedLen = 0;
		int32_t i;
		for (i = 0; i < singleLen; i++)
		{
				dataEncoded[i] = (int8_t)(singlePageData[i] ^ writeKey);
				dataEncodedLen += 1;
		}
		//
		if (dataEncodedLen == singleLen)
		{
				return dataEncoded;
		}
		else
		{
				free(dataEncoded);
				return NULL;
		}
}


//�����ݽ��ܺ�����TBC
int8_t* readDecode(int8_t* singlePageData, int32_t singleLen, int8_t readKey)
{
    //��ʵwirteEncode��readDecode�Ĺ�����һ���ģ�ֻ��һ����WK,һ��RK
    //byte[] dataDecoded = new byte[singlePageData.Length];
    int8_t* dataDecoded = (int8_t*)malloc(sizeof(int8_t) * singleLen);
    //�ж�ҳ�����Ƿ���Ч
    if ((singleLen != 4) && (singleLen != 2))
    {
        //System.Console.WriteLine("������ҳ�������󣬽���ʧ��");
//        printf("������ҳ�������󣬽���ʧ��\n");
				free(dataDecoded);
        return NULL;
    }
    else
    {
        int32_t dataDecodedLen = 0;
			  int32_t i;
        for (i = 0; i < singleLen; i++)
        {
            dataDecoded[i] = (int8_t)(singlePageData[i] ^ readKey);
            dataDecodedLen += 1;
        }
        if (dataDecodedLen == singleLen)
        {
            return dataDecoded;
        }
        else
        {
            //System.Console.WriteLine("����ʧ��");
    //        printf("����ʧ��\n");
						free(dataDecoded);
            return NULL;
        }
        //        
    }
}

//�Ӳ�ֵ���㺯��
int8_t* EnInterpolation(int8_t itspv, int8_t Data[], int32_t dataLen, int8_t RAND[], int32_t randLen)
{
    //��itspת��Ϊint���͵�ƫ����
    //int[] itsp = new int[4];
   // printf("����11 == %02X \n", itspv);
    int32_t itsp[4] = { 0,0,0,0 };
		int32_t j;
    for (j = 3; j > -1; j--)
    {
        itsp[j] = (int32_t)((itspv & (0x03 << (j * 2))) >> (j * 2));
        if ((itsp[j] == 0) && (j == 3))
        {
            //          printf("ITSP���ݲ��Ϲ棬��λΪ0��\n");
        }
        else if ((itsp[j] == 0) && (j != 3))
        {
            itsp[j] = itsp[j + 1];
        }
    }


    struct mDataInt* newData = byteToBinary(Data, dataLen);
    struct mDataInt* newRAND = byteToBinary(RAND, randLen);


    int32_t flag = itsp[3];

    //ת����List<int>����
    int32_t* reverseData = reverseArrayforInt(newData->data, newData->len);  //��һ�η�ת

    int32_t list[32] = { -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0 };
		int32_t n;
    for (n = 0; n < newData->len; n++) {
        list[n] = reverseData[n];
    }


    //����
    int32_t i = 0;
    int32_t count = 0;
    for (; (flag - i < 16) && (count < 32);)
    {
				int32_t k;
        for (k = 31; k > flag; k--) {
            list[k] = list[k - 1];
        }

        //ѡ�ƶ��Ժ��ٸ�ֵ
        list[flag] = newRAND->data[i];


        count += 1;
        i++;
        flag = flag + itsp[3 - (i % 4)] + 1;

    }
    count = newData->len + i;
    //����һ�¿յ�
    while (count < 32)
    {
        list[count] = newRAND->data[i];
        count += 1;
        i++;
    }

    int32_t* reverseDataAIp = reverseArrayforInt(list, 32);   //��֤��ȷ

    //�����ò�ֵ��int����ת����byte����
    //������32���ȵ�����
    struct mDataint8_t* byteAfterIp = binaryToByte(reverseDataAIp, 32);

    int8_t* reversedByteAfterIp = reverseArrayforint8_t(byteAfterIp->data, byteAfterIp->len);

    free(byteAfterIp->data);
    free(byteAfterIp);
    free(reverseDataAIp);
    free(newData->data);
    free(newData);
    free(newRAND->data);
    free(newRAND);
    return reversedByteAfterIp;

}



//ȥ��ֵ���㺯��
struct mDataint8_t* DeInterpolation(int8_t ITSP, int8_t Data[], int32_t dataLen)
{
    //��itspת��Ϊint���͵�ƫ����
    //int[] itsp = new int[4];
    //int32_t* itsp = malloc((sizeof(int32_t) * 4));
	  int32_t itsp[4] = {0x00,0x00,0x00,0x00};
		int32_t j;
    for (j = 3; j > -1; j--)  //ITSP[]=[0,1,2,3], itsp[]=[3,2,1,0]
    {
        itsp[j] = (int32_t)((ITSP & (0x03 << (j * 2))) >> (j * 2));
        if ((itsp[j] == 0) && (j == 3))
        {
            // printf("ITSP���ݲ��Ϲ棬��λΪ0��");

        }
        else if ((itsp[j] == 0) && (j != 3))
        {
            itsp[j] = itsp[j + 1];
        }
    }


    int8_t* reversedData = reverseArrayforint8_t(Data, dataLen);
    struct mDataInt* newData = byteToBinary(reversedData, dataLen);

    int32_t* reverseData = reverseArrayforInt(newData->data, newData->len);
    int32_t flag = itsp[3];
    //ȥ��ֵ
    int32_t i = 0;
    int32_t count = 0;  //ԭ�ļ�����Ӧ��16λ
    int32_t* dataAIp = malloc(sizeof(int32_t) * 16);
    for (; count < 16;)
    {
        if (i != flag)
        {
            dataAIp[count] = reverseData[i];
            count++;
        }
        else
        {
            flag = flag + itsp[3 - ((i + 1 - count) % 4)] + 1;
        }
        i++;
    }
    //��ȥ��ֵ��int����ת����byte����
    int32_t* reversedDataAIp = reverseArrayforInt(dataAIp, 16);

    struct mDataint8_t* byteAfterIp = binaryToByte(reversedDataAIp, 16);
//    struct mDataint8_t* retData = malloc(sizeof(struct mDataint8_t));

//    retData->data = byteAfterIp->data;
//    retData->len = byteAfterIp->len;

    free(reversedDataAIp);
    free(reversedData);
    free(reverseData);
    free(newData->data);
    free(newData);
    free(dataAIp);
		
    return byteAfterIp;
}





//�ֽ�ת2����
struct mDataInt* byteToBinary(int8_t HEX[], int32_t hexLen)
{
    int32_t length = hexLen * 8;
    struct mDataInt* newData = malloc(sizeof(struct mDataInt));
    int32_t* BinaryArray = malloc(sizeof(int32_t) * length);
    int32_t iv = 0x80;
		int32_t i;
    for (i = 0; i < length; i++)
    {
        if (i % 8 == 0)
        {
            iv = 0x80;
        }
        if ((HEX[i / 8] & iv) == 0)
        {
            BinaryArray[i] = 0;
        }
        else
        {
            BinaryArray[i] = 1;
        }
        iv = iv >> 1;
    }

    newData->data = BinaryArray;
    newData->len = length;
    return newData;
}


//2��������תbyte����
struct mDataint8_t* binaryToByte(int32_t BIN[], int32_t binLen)
{
    int32_t length = binLen / 8;
    if (binLen < 1) {
        return NULL;
    }
    int8_t* ByteArray = (int8_t*)malloc(sizeof(int8_t) * length);
		int32_t i;
    for (i = 0; i < length; i++)
    {
        ByteArray[i] = (int8_t)((8 * BIN[i * 8] + 4 * BIN[i * 8 + 1] + 2 * BIN[i * 8 + 2] + BIN[i * 8 + 3]) * 16 + (8 * BIN[i * 8 + 4] + 4 * BIN[i * 8 + 5] + 2 * BIN[i * 8 + 6] + BIN[i * 8 + 7]));
    }

    struct mDataint8_t* retData = malloc(sizeof(struct mDataint8_t));
    retData->data = ByteArray;
    retData->len = length;
    return retData;
}


//�ַ�ת�ֽ�
struct mDataint8_t* stringToBytes(int8_t pageStrData[], int32_t length)
{
    int32_t byteLength = length / 2;
    int8_t* pageByteData = (int8_t*)malloc(sizeof(int8_t) * byteLength);
    int32_t high, low;
    if (pageStrData != NULL)
    {
				int32_t i;
        for (i = 0; i < byteLength; i++)
        {
            //ÿ��λ��ȡ����ת��string��ʽ��byte
            if (pageStrData[i * 2] >= '0' && pageStrData[i * 2] <= '9')
                high = pageStrData[i * 2] - '0';
            else if (pageStrData[i * 2] >= 'A' && pageStrData[i * 2] <= 'F')
                high = pageStrData[i * 2] - 'A' + 10;
            else if (pageStrData[i * 2] >= 'a' && pageStrData[i * 2] <= 'f')
                high = pageStrData[i * 2] - 'a' + 10;
            else
            {
                return NULL;
            }

            if (pageStrData[i * 2 + 1] >= '0' && pageStrData[i * 2 + 1] <= '9') {
                low = pageStrData[i * 2 + 1] - '0';
            }
            else if (pageStrData[i * 2 + 1] >= 'A' && pageStrData[i * 2 + 1] <= 'F') {
                low = pageStrData[i * 2 + 1] - 'A' + 10;
            }
            else if (pageStrData[i * 2 + 1] >= 'a' && pageStrData[i * 2 + 1] <= 'f') {
                low = pageStrData[i * 2 + 1] - 'a' + 10;
            }
            else
            {
                return NULL;
            }
            pageByteData[i] = (int8_t)(high * 16 + low);;
        }
    }


    struct mDataint8_t* newData = malloc(sizeof(struct mDataint8_t));

    newData->data = pageByteData;
    newData->len = byteLength;
    return newData;
}



//��ת����
int32_t* reverseArrayforInt(int32_t inputArray[], int32_t len)
{
    int32_t length = len;
    int32_t* reversedArray = (int32_t*)malloc(sizeof(int32_t) * length);
		int32_t i;
    for (i = 0; i < length; i++)
    {
        reversedArray[i] = inputArray[length - i - 1];
    }
    return reversedArray;
}


//��ת����˳��
int8_t* reverseArrayforint8_t(int8_t* inputArray, int32_t len)
{
    int32_t length = len;
    int8_t* reversedArray = (int8_t*)malloc(sizeof(int8_t) * length);
		int32_t i;
    for (i = 0; i < length; i++) {
        reversedArray[i] = inputArray[length - i - 1];
    }

    return reversedArray;

}

