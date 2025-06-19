#!/system/bin/sh
scene="com.dnagda.eliteG"

SOURCE_DAEMON="/storage/emulated/0/Android/data/$scene/daemon"
SOURCE_TOYBOX="/storage/emulated/0/Android/data/$scene/toolkit/toybox-outside64"
SOURCE_BUSYBOX="/storage/emulated/0/Android/data/$scene/toolkit/busybox"

STARTER_PATH="/data/local/tmp/eliteg-daemon"
TARGET_TOYBOX="/data/local/tmp/toolkit/toybox-outside"
TARGET_BUSYBOX="/data/local/tmp/toolkit/busybox"
toolkit="/data/local/tmp/toolkit"

mkdir -p $toolkit
export PATH=$PATH:$toolkit

if [ -f "$SOURCE_DAEMON" ]; then
    cp "$SOURCE_DAEMON" "$STARTER_PATH"
    chmod 777 "$STARTER_PATH"
    echo 'Success: Copy [eliteg-daemon] to complete'
else
    echo 'daemon file not found!'
    exit 1
fi

if [ -f "$SOURCE_TOYBOX" ]; then
    cp "$SOURCE_TOYBOX" "$TARGET_TOYBOX"
    chmod 777 "$TARGET_TOYBOX"
    echo 'Success: Copy [toybox-outside] to complete'
else
    echo 'toybox-outside file not found!'
fi

if [ -f "$SOURCE_BUSYBOX" ]; then
    cp "$SOURCE_BUSYBOX" "$TARGET_BUSYBOX"
    chmod 777 "$TARGET_BUSYBOX"
    echo 'Success: Copy [busybox] to complete'
else
    echo 'busybox file not found!'
fi

echo 'Install BusyBox……'
cd $toolkit
if [ -f "./busybox" ]; then
    for applet in ./busybox --list; do
      case "$applet" in
      "sh"|"busybox"|"shell"|"swapon"|"swapoff"|"mkswap")
        echo '  Skip' > /dev/null
      ;;
      *)
        ./busybox ln -sf busybox "$applet";
      ;;
      esac
    done
    ./busybox ln -sf busybox busybox_1_30_1
else
    echo 'BusyBox not found, skipping applet links.'
fi

echo ''
nohup "$STARTER_PATH" >/dev/null 2>&1 &
if [[ $(pgrep eliteg-daemon) != "" ]]; then
  echo 'EliteG-Daemon OK! ^_^'
else
  echo 'EliteG-Daemon Fail! @_@'
fi

cmd package compile -m speed $scene >/dev/null 2>&1 &
dumpsys deviceidle whitelist +$scene >/dev/null 2>&1
cmd appops set $scene RUN_IN_BACKGROUND allow >/dev/null 2>&1
echo ''
