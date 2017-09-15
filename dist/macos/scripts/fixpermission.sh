#!/bin/bash

# The launch script loses its executable permission when pkgbuild creates the flat installation package,
# so we need to set as executable again after in the post-install step
/bin/chmod +x /Applications/Mirador.app/Contents/MacOS/Mirador