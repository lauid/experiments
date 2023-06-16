#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>

#define GPIO1_BASE_ADDR 0x0209C000 // GPIO1的基地址
#define GPIO1_SIZE      0x4000     // GPIO1的大小

#define GPIO_PIN        18         // 要控制的GPIO引脚

int main(int argc, char **argv)
{
    int fd;
    void *gpio1_base;
    unsigned int *gpio1_dir, *gpio1_data;

    // 打开/dev/mem文件，以读写方式打开
    fd = open("/dev/mem", O_RDWR | O_SYNC);
    if (fd < 0) {
        perror("open /dev/mem");
        exit(1);
    }

    // 映射GPIO1的物理地址到用户空间
    gpio1_base = mmap(NULL, GPIO1_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd, GPIO1_BASE_ADDR);
    if (gpio1_base == MAP_FAILED) {
        perror("mmap");
        exit(1);
    }

    // 得到GPIO1的方向寄存器和数据寄存器的指针
    gpio1_dir = (unsigned int *)(gpio1_base + 0x400);
    gpio1_data = (unsigned int *)(gpio1_base + 0x3FC);

    // 设置GPIO1_PIN为输出
    *gpio1_dir |= (1 << GPIO_PIN);

    // 输出高电平
    *gpio1_data |= (1 << GPIO_PIN);

    // 等待1秒
    sleep(1);

    // 输出低电平
    *gpio1_data &= ~(1 << GPIO_PIN);

    // 解除GPIO1的物理地址映射
    munmap(gpio1_base, GPIO1_SIZE);

    // 关闭/dev/mem文件
    close(fd);

    return 0;
}

