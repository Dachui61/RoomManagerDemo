package com.lidaxin.data;

/**
 * @author benjaminwan
 *数据转换工具
 */
public class MyFunc {
	//-------------------------------------------------------
	// 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
	static public int isOdd(int num)
	{
		return num & 0x1;
	}
	//-------------------------------------------------------
	static public int HexToInt(String inHex)//Hex字符串转int
	{
		return Integer.parseInt(inHex, 16);
	}
	//-------------------------------------------------------
	static public byte HexToByte(String inHex)//Hex字符串转byte
	{
		return (byte) Integer.parseInt(inHex,16);
	}
	//-------------------------------------------------------
	static public String Byte2Hex(Byte inByte)//1字节转2个Hex字符
	{
		return String.format("%02x", inByte).toUpperCase();
	}
	//-------------------------------------------------------
	static public String ByteArrToHex(byte[] inBytArr)//字节数组转转hex字符串
	{
		StringBuilder strBuilder=new StringBuilder();
		int j=inBytArr.length;
		for (int i = 0; i < j; i++)
		{
			strBuilder.append(Byte2Hex(inBytArr[i]));
//			strBuilder.append(" ");
		}
		return strBuilder.toString();
	}
	//-------------------------------------------------------
	static public String ByteArrToHex(byte[] inBytArr, int offset, int byteCount)//字节数组转转hex字符串，可选长度
	{
		StringBuilder strBuilder=new StringBuilder();
		int j=byteCount;
		for (int i = offset; i < j; i++)
		{
			strBuilder.append(Byte2Hex(inBytArr[i]));
		}
		return strBuilder.toString();
	}
	static public String ByteArrToInt(byte[] inBytArr)//字节数组转转hex字符串，可选长度
	{
		StringBuilder strBuilder=new StringBuilder();
		for (int i = 0; i < inBytArr.length; i++)
		{
			strBuilder.append(inBytArr[i]);
		}
		return strBuilder.toString();
	}

	static public String IntToHex(int number){
		int i = 0;
		StringBuilder sb = new StringBuilder();
		char[] S = new char[100];
		if(number == 0){
			sb.append("0");
		}
		else{
			while(number!=0)
			{
				int t=number%16;
				if(t >=0 && t<10)
				{
					S[i] = (char)(t+'0');
					i++;
				}
				else
				{
					S[i] = (char)(t+'A'-10);
					i++;
				}
				number=number/16;
			}
			for (int j=i-1;j>=0;j--) {
				sb.append(S[j]);
			}
		}
		return sb.toString();
	}

	//-------------------------------------------------------
	//转hex字符串转字节数组
	static public byte[] HexToByteArr(String inHex)//hex字符串转字节数组
	{
		int hexlen = inHex.length();
		byte[] result;
		if (isOdd(hexlen)==1)
		{//奇数
			hexlen++;
			result = new byte[(hexlen/2)];
			inHex="0"+inHex;
		}else {//偶数
			result = new byte[(hexlen/2)];
		}
		int j=0;
		for (int i = 0; i < hexlen; i+=2)
		{
			result[j]=HexToByte(inHex.substring(i,i+2));
			j++;
		}
		return result;
	}
}