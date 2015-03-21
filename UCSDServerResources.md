# Introduction #

We have a couple of public servers at SDSC/UCSD, these pages detail what we have, IPs, machine types, configurations, etc.

Pictures at http://dataturbine.org/content/new-dell-1435-server-calit2

## niagara.sdsc.edu ##

Sun T2000, 32 cores, 16GB of memory, quad gigabit ethernet, with a Caen 9TB SATA-to-SCSI RAID chassis. Physically located in Calit2 (Atkinson) Hall, first floor machine room. Pictures to be posted.

niagara has 4 IPs on 4 different interfaces:
  * niagara Main, production use
  * niagara-stage.sdsc.edu Mid stage between development and production
  * niagara-dev.sdsc.edu Development instance, may be down at any time

All have DataTurbine instances running on them, 4GB of memory each. All have the RAID chassis for archiving, which means that they can hold a **lot** of data.

**IPs here!**

OS is Solaris 10 2/08, with the RAID chassis formatted as a single ZFS container.

To-do
  * Format the second 73G system disk - mirror? More space? Backup?

## iguassu.sdsc.edu ##

Dell PowerEdge 1435, 2 dual-core 2.0GHz Opteron CPUs, 6GB of memory, 2 gigabit ports, 2x internal 750GB SATA drive. Installed Debian 4.0 3/24/08, to be configured.

(Named for [Iguassu Falls](http://en.wikipedia.org/wiki/Iguassu_Falls), in South America)

> IP 137.110.118.250, gateway .1, class C.
> DNS 132.239.1.52, 128.54.16.2

To-do
  * OS reinstall
  * Account setup
  * Install of RBNB Rocks roll

## phalanx.sdsc.edu ##
Dell Precision 470, dual Intel Xeon 3GHz, 1G memory, 500G storage, RHEL 4

137.110.118.150

Primary usage is as a backup RBNB dev server.

Host to legacy cvs repository of CLEOS.

## devastator.sdsc.edu ##
Dell PWS390, core2 quad 2.66GHz, 3.5G memory, 296G storage, MS Windows Vista

137.110.118.52

Primary uses are LabVIEW development and portability testing for RBNB and its applications

## 137.110.118.246 (no dns name) ##
Dell Precision 450, dual Xeon 2.4 GHz, 2G memory, 300G storage, Debian Linux 4.0

Generic Linux system