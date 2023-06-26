root@node1:~/code/experiments/linux/misc# make test
# We put a — in front of the rmmod command to tell make to ignore
# an error in case the module isn’t loaded.
sudo rmmod misc
# Clear the kernel log without echo
sudo dmesg -C
# Insert the module
sudo insmod misc.ko
# Display the kernel log
dmesg
[ 3171.162830] misc_register succeed!!!
root@node1:~/code/experiments/linux/misc# lsmod | grep misc
misc                   16384  0
binfmt_misc            24576  1
root@node1:~/code/experiments/linux/misc# ll /dev/hello_misc
crw------- 1 root root 10, 57 Jun 21 13:50 /dev/hello_misc
