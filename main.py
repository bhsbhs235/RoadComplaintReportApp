from bluetooth import *

import time

import socket

from picamera import PiCamera

import io

import os

import sys

import threading

import json

import bluetooth._bluetooth as bluez

import blescan

from datetime import datetime

from time import sleep

import shlex

import subprocess

import os.path

 

flagA = 0

flagB = 0

cv = threading.Condition()

a = "86df8143c14c199d7e5efcf3d8210eb8"

 

class Format:

    def __init__(self,what,cameracnt,videosec,beacon_list):

        self.what = what

        self.cameracnt = cameracnt

        self.videosec = videosec

        self.beacon_list = beacon_list

    def __str__(self):

        sentencea = ""

        for x in beacon_list:

            sentencea = sentencea + '{"uuid":"' + x["uuid"] + '"}' + ','

        length = len(sentencea)-1

        sentence = sentencea[0:length]

        return '{"what":%d,"camera":{"count":%d},"video":{"seconds":%d},"beacon":[%s]}' %(self.what,self.cameracnt,self.videosec,sentence)

 

with open("data1.json","r") as data_file:

    data = json.load(data_file)

    what = data["what"]

    cameracnt = data["camera"]["count"]

    videosec = data["video"]["seconds"]

    beacon_list = data["beacon"]

    format = Format(what,cameracnt,videosec,beacon_list)

 

camera = PiCamera()

camera.rotation = -90

camera.start_preview()

 

server_sock=BluetoothSocket( RFCOMM )

server_sock.bind(("",PORT_ANY))

server_sock.listen(1)

 

port = server_sock.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

advertise_service( server_sock, "SampleServer",

                   service_id = uuid,

                   service_classes = [uuid, SERIAL_PORT_CLASS],

                   profiles = [SERIAL_PORT_PROFILE],

                   )

 

print("waiting for connection on RFCOMM channel1 %d" % port)

 

client_sock,client_info = server_sock.accept()

print("Accepted connection from ", client_info)

 

client_sock.send("jsondata")

sleep(2)

client_sock.send(str(format))

 

class Video(threading.Thread):

 

    def run(self):

        global flagA

        global flagB

        while True:

            for i in range(2):

                seconds = format.videosec

                seconds = seconds/3

                print '###seconds-----> ' + str(seconds)

                camera.start_recording('temp.h264')

                sleep(seconds)

                camera.stop_recording()

                print 'sleep %d ' %seconds

                if flagA == 0:

                    print 'flagA = '+str(flagA)

                elif flagA == 1:

                    h264tomp4()

                    if i == 0:

                        i0()

                    elif i == 1:

                        i1()

                    client_sock.send("datasend")

                    sleep(7)

                    fd = open("happy360.mp4","rb")

                    data = bytearray(fd.read())

                    t = buffer(data,0)

                    print len(t)

                    datasize = sys.getsizeof(data)

                    sleep(3)

                    client_sock.send(str(len(t)))

                    sleep(3)

                    client_sock.send(t)

                    fd.close()

                    flagA = 0

                    cv.notify()

                    cv.release()

                else:

                    print 'error error error'

 

                sentence = "video" + str(i) + ".h264"

                print sentence

                rename_files("/home/pi/iBeacon-Scanner-",sentence)

 

                if flagB == 0:

                    print 'flagB = ' + str(flagB)

                elif flagB == 1:

                    cv.acquire()

                    flagB = 0

                    print 'video thread turn flagB = ' + str(flagB)

                    flagA = 1

                    print 'video thread turn flagA = ' + str(flagA)

                   # cv.notify()

                   # cv.release()

                else:

                    print 'error2 error2 error2'

 

class Bcscan(threading.Thread):

 

    dev_id = 0

    global socks

 

    try:

        socks = bluez.hci_open_dev(dev_id)

        print "scan started"

    except:

        print "Bcscan error"

        sys.exit(1)

 

    blescan.hci_le_set_scan_parameters(socks)

    blescan.hci_enable_le_scan(socks)

 

    def run(self):

        global a

        global flagB

        flagC = 0

        global format

        while True:

            returnedList = blescan.parse_events(socks,3)

            print "-------------------------"

            for beacon in returnedList:

                print "*****************************"

                if beacon[18:50] == a:

                    print "please~~~~~~~~~~~~~~~~~~~~"

                    flagC = 1

            if flagC == 1:

                print 'flagC = '+str(flagC)

                if format.what== 0:

                    cv.acquire()

                    flagB = 1

                    print 'flagB = '+str(flagB)

                    cv.wait()

                    #cv.notify()

                    cv.release()

                else:

                    print 'capture start'

                    i = format.cameracnt

                    client_sock.send("datasend")

                   # sleep(4)

                    while i > 0:

                        sentence = "/home/pi/Pictures/capture" + str(i) + ".png"

                        print sentence

                        camera.capture(sentence)

                        i-=1

                    i = format.cameracnt

                    while i > 0:

                        sentence = "/home/pi/Pictures/capture" + str(i) + ".png"

                        fd = open(sentence,"rb")

                        data = bytearray(fd.read())

                        t = buffer(data,0)

                        print(sys.getsizeof(data))

                        client_sock.send(t)

                        fd.close()

                        i-=1

                    print "image send success"

            flagC = 0

 

class Receive(threading.Thread):

    def run(self):

        global a

        global format

        try:

            while True:

                receiveddata = client_sock.recv(1024)

                if receiveddata > 0:

                    action = str(receiveddata)

                    print action

                if action == "preferences":

                    print 'action === preferences'

                    data = client_sock.recv(1024)

                    a = int(data)

                    if a == 1:

                        format.what = 1

                        fwrite()

                    elif a == 2:

                        format.what = 0

                        fwrite()

                    elif a == 3:

                        format.cameracnt = 3

                        fwrite()

                    elif a == 4:

                        format.cameracnt = 5

                        fwrite()

                    elif a == 5:

                        format.cameracnt = 8

                        fwrite()

                    elif a == 6:

                        format.videosec = 45

                        fwrite()

                    elif a == 7:

                        format.videosec = 60

                        fwrite()

                    elif a== 8:

                        format.videosec = 90

                        fwrite()

                elif action == "uuid":

                    print 'action === uuid'

                    data = client_sock.recv(1024)

                    uuid = str(data)

                    a = uuid

                    print uuid

                elif action == "uuidadd":

                    print 'action === uuidadd'

                    data = client_sock.recv(1024)

                    uuidadd = str(data)

                    json_str = '{"uuid":"' + uuidadd +'"}'

                    print json_str

                    uuid_object = json.loads(json_str)

                    print uuid_object

                    format.beacon_list.append(uuid_object)

                    print format.beacon_list

                    fwrite()

        except:

            pass

 

def rename_files(directory,sentence):

    file_names = os.listdir(directory)

    save_dir = os.getcwd()

    os.chdir(directory)

    for name in file_names:

        if name.startswith('temp.h264'):

            newname = name.replace("temp.h264",sentence)

            os.rename(name,newname)

            print name + '->' + newname

    os.chdir(save_dir)

 

def fwrite():

    with open("data1.json","w") as data_file:

        data_file.write(str(format))

        print format

 

def h264tomp4():

    command1 = ["ffmpeg","-y","-i","video0.h264","video0.mp4"]

    command2 = ["ffmpeg","-y","-i","video1.h264","video1.mp4"]

    command3 = ["ffmpeg","-y","-i","temp.h264","temp.mp4"]

    subprocess.call(command1)

    subprocess.call(command2)

    subprocess.call(command3)

 

def i0():

    command4 = ["ffmpeg","-y","-f","concat","-i","i0.txt","-c","copy","happy.mp4"]

    command5 = ["ffmpeg","-i","happy.mp4","-y","-vf","scale=320:180","happy360.mp4"]

    subprocess.call(command4)

    subprocess.call(command5)

 

def i1():

    command4 = ["ffmpeg","-y","-f","concat","-i","i1.txt","-c","copy","happy.mp4"]

    command5 = ["ffmpeg","-i","happy.mp4","-y","-vf","scale=320:180:","happy360.mp4"]

    subprocess.call(command4)

    subprocess.call(command5)

 

Video().start()

Bcscan().start()

Receive().start()
