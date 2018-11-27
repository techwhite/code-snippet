#!/usr/bin/python
#coding:utf-8

import base64
import sys

def convert():
    f=open('./threadstate.jpg','rb') #二进制方式打开图文件
    ls_f=base64.b64encode(f.read()) #读取文件内容，转换为base64编码
    f.close()
    print(ls_f)

if __name__ == '__main__':
    reload(sys)
    sys.setdefaultencoding('utf-8')
    convert
