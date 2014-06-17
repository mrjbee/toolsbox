#!/usr/bin/python
import os                                                                                                                                                                                                                                    
import datetime                                                                                                                                                                                                                              
import sys
import requests
from requests.auth import HTTPBasicAuth
from requests.exceptions import Timeout
import time

global serverUrl 
serverUrl = "http://194.29.62.160:8880/remote-config-1.0-SNAPSHOT/rest"
global basicAuth
basicAuth = HTTPBasicAuth('rest', 'rest')
global defaultTimeOut
defaultTimeOut=10
requests.adapters.DEFAULT_RETRIES = 3

def exitWithError():
	print "ERROR: Something goes wrong going to abort script."
	sys.exit(1)


print "Remote rtcwake script [%s] ...."%(time.strftime("%c"))

print "Using remote server: [%s]" % (serverUrl)

print "Connection check ...."
try:
	response = requests.get(serverUrl+"/secure-ping", auth=basicAuth, timeout=defaultTimeOut)
	if response.status_code == 200:
		print " --- [sucess]"
	else:	
		print " --- [fails with %s status]"%(response.status_code)
		exitWithError()
except (Timeout):
	print "Sorry no connection to server... try again later."
	exitWithError()


print "Fetching sleep timeout ...."
try:
	response = requests.get(serverUrl+"/server/moon/sleepminutes", auth=basicAuth, timeout=defaultTimeOut)
	if response.status_code == 200:
		sleepminutes = int(response.text)
	else:	
		print " --- [fails with %s status]"%(response.status_code)
		exitWithError()
except (Timeout):
	print "Sorry no connection to server... try again later."
	exitWithError()
print " --- [sucess] Going to sleep for %s minutes"%(sleepminutes)	

onlineStatus = "Online"
if sleepminutes!=0:
	onlineStatus = "Offline" 

print "Updating status [%s]...."%(onlineStatus)
try:
	response = requests.post(serverUrl+"/server/moon/status", auth=basicAuth, timeout=defaultTimeOut, data=onlineStatus)
	if response.status_code == 200:
		print " --- [sucess]"
	else:	
		print " --- [fails with %s status]"%(response.status_code)
		exitWithError()
except (Timeout):
	print "Sorry no connection to server... try again later."
	exitWithError()

if sleepminutes == 0:
	print "No sleep requested. Going to exit"
	sys.exit(0)
	
sleepminutes = 2
print "Sleep requested. Going to sleep for %s minutes" % (sleepminutes)
bashCommand = "sudo rtcwake -s %s -m off"%(sleepminutes*60)
os.system(bashCommand)