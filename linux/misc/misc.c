#include <linux/init.h>
#include <linux/module.h>
#include <linux/miscdevice.h>
#include <linux/fs.h>

struct file_operations misc_fops = {
    .owner = THIS_MODULE};

struct miscdevice misc_dev = {
    .minor = MISC_DYNAMIC_MINOR,
    .name = "hello_misc",
    .fops = &misc_fops
};

static int misc_init(void)
{
    int ret;

    ret = misc_register(&misc_dev);

    if (ret < 0)
    {
        printk("misc_register failed!!!\n");
        return -1;
    }

    printk("misc_register succeed!!!\n"); // 在内核中无法使用c语言库，所以不用printf

    return 0;
}

static void misc_exit(void)
{
    misc_deregister(&misc_dev);

    printk("misc exit!!!\n");
}

module_init(misc_init);
module_exit(misc_exit);

MODULE_LICENSE("GPL"); // 声明模块拥有开源许可
