#!/bin/sh

[ -z "${ACTIVATIONKEY}" ] && {
  echo "assign your RHN activation key in a variable ACTIVATIONKEY, export it and re-run the script."
  exit 1
}

rhnreg_ks --activationkey="${ACTIVATIONKEY}"
yum update -y

sed -i -e /^HOSTNAME=/d /etc/sysconfig/network
sed -i -e /^HWADDR=/d /etc/sysconfig/network-scripts/ifcfg-eth0

rm -f /etc/sysconfig/rhn/systemid 
rm -f /etc/udev/rules.d/70-persistent-*
rm -f /etc/ssh/ssh_host_*

service lvcd stop
rm -vrf /root/.ssh/

> /var/log/messages
> /var/log/cron
> /var/log/audit/audit.log 

find /var/log/  -regex "/var/log/.*-[0-9]+" -exec rm -f {} \;
rm -f /var/log/dmesg.old

sync
> .bash_history
history -c
sync

sleep 10
poweroff; history -c
