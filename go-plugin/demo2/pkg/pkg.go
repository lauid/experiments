package pkg

import (
	"log"
	"plugin"
)

func init() {
	log.Println("pkg init")
}

func LoadPlugin(pluginPath string) error {
	_, err := plugin.Open(pluginPath)
	if err != nil {
		return err
	}
	return nil
}
