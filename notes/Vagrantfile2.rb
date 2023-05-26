Vagrant.configure("2") do |config|
  # 配置第一个虚拟机
  config.vm.define "vm1" do |vm1|
    vm1.vm.box = "ubuntu/xenial64"
    vm1.vm.hostname = "vm1"
    vm1.vm.network "private_network", ip: "192.168.33.10"
    vm1.vm.provider "virtualbox" do |v|
      v.memory = 1024
    end
  end

  # 配置第二个虚拟机
  config.vm.define "vm2" do |vm2|
    vm2.vm.box = "ubuntu/xenial64"
    vm2.vm.hostname = "vm2"
    vm2.vm.network "private_network", ip: "192.168.33.11"
    vm2.vm.provider "virtualbox" do |v|
      v.memory = 2048
    end
  end
end
