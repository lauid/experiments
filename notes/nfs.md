win10家庭版使用nfs https://github.com/winnfsd/winnfsd
umount -lf /mnt
root@phyboard-mira-imx6-3:~# mount -t nfs 191.168.0.12:/c/share /mnt/ -o nfsvers=3