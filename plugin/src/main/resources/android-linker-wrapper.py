import os
import subprocess
import sys

cmdline = [
	os.environ["RGP_CC"],
	os.environ["RGP_CC_LINK_ARGS"],
]
cmdline.extend(sys.argv[1:])

ndk_major_version = int(os.environ["RGP_NDK_MAJOR_VERSION"])

# This is a compatibility wrapper for using mismatched rustc and NDK versions
# https://blog.rust-lang.org/2023/01/09/android-ndk-update-r25.html
def fixArgs(cmdline):
	# Starting with NDK r23, the `libgcc` is no longer included, link against `libunwind` instead
	if ndk_major_version >= 23:
		return cmdline.replace("-lgcc", "-lunwind")
	# For previous versions, we have to force linking against libgcc (Rust 1.68+ defaults to libunwind)
	# This isn't really supposed to be done
	else:
		return cmdline.replace("-lunwind", "-lgcc")

for i, arg in enumerate(cmdline):
	cmdline[i] = fixArgs(arg)

	# Check if a linker arguments file in the format of `@<ABSOLUTE_PATH>`
	# This has to be scanned for arguments too
	if arg.startswith("@"):
		f = open(arg[1:], "r").read()
		f = fixArgs(f)
		open(arg[1:], "w").write(f)

# To help with debugging errors. This only appears when build fails.
print("cmdline: " + str(cmdline))

sys.exit(subprocess.call(cmdline))
