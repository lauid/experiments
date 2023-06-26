#include <linux/init.h>
#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/gpio.h>

#define LED_GPIO_PIN 17

static int __init led_driver_init(void)
{
    int ret;

    printk(KERN_INFO "LED Driver: Initializing\n");

    ret = gpio_request(LED_GPIO_PIN, "LED");
    if (ret < 0)
    {
        printk(KERN_ERR "LED Driver: Failed to request GPIO\n");
        return ret;
    }

    gpio_direction_output(LED_GPIO_PIN, 1); // 设置为输出模式并打开LED灯

    printk(KERN_INFO "LED Driver: LED On\n");

    return 0;
}

static void __exit led_driver_exit(void)
{
    printk(KERN_INFO "LED Driver: Exiting\n");

    gpio_set_value(LED_GPIO_PIN, 0); // 关闭LED灯
    gpio_free(LED_GPIO_PIN);
}

module_init(led_driver_init);
module_exit(led_driver_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Your Name");
MODULE_DESCRIPTION("LED Driver");
