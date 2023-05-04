package config

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestConfig(t *testing.T) {
	assert.NotPanics(t, func() {
		readConfig()
	})
}
