#!/usr/bin/env python2.7

import os
import shutil
import urllib2

version = 7
update = 60
build = 19

filename = 'jre-%du%d-windows-i586.tar.gz' % (version, update)
#filename = 'jre-7u60-windows-x64.tar.gz'
folder = 'jre1.%d.0_%d' % (version, update)  # jre1.7.0_60

cookie = 'gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie'
url = 'http://download.oracle.com/otn-pub/java/jdk/%du%d-b%d/%s' % (version, update, build, filename)

jre_optional_windows = [
  'bin/dtplugin',
  'bin/plugin2',

  'bin/kinit.exe',
  'bin/klist.exe',
  'bin/ktab.exe',

  'bin/keytool.exe',
  'bin/orbd.exe',
  'bin/policytool.exe',
  'bin/rmid.exe',
  'bin/rmiregistry.exe',
  'bin/servertool.exe',
  'bin/tnameserv.exe',

  'bin/javaws.exe',
  'lib/javaws.jar',

  'lib/ext/dnsns.jar',
  'lib/cmm/PYCC.pf',
]

javafx_basics = [
  'THIRDPARTYLICENSEREADME-JAVAFX.txt',
  'lib/javafx.properties',
  'lib/jfxrt.jar',
  'lib/security/javafx.policy'
]

javafx_windows = [
  'bin/decora-sse.dll',
  'bin/fxplugins.dll',
  'bin/glass.dll',
  'bin/glib-lite.dll',
  'bin/gstreamer-lite.dll',
  'bin/javafx-font.dll',
  'bin/javafx-iio.dll',
  'bin/jfxmedia.dll',
  'bin/jfxwebkit.dll',
  'bin/libxml2.dll',
  'bin/libxslt.dll'
]

if not os.path.exists('jre.tgz'):
   opener = urllib2.build_opener()
   opener.addheaders.append(('Cookie', cookie))
   source = opener.open(url)

   print 'Downloading %s...' % (filename)
   target = open('jre.tgz', 'wb')
   target.write(source.read())

   source.close()
   target.close()

   # remove any old 'java' folder that might be there
   if os.path.exists('java'):
      shutil.rmtree('java')

if not os.path.exists('java'):
   os.system('tar xvfz jre.tgz')
   os.rename(folder, 'java')

   for filename in jre_optional_windows + javafx_basics + javafx_windows:
      path = 'java/' + filename
      print 'removing ' + path
      if os.path.isdir(path):
         shutil.rmtree(path)
      else:
         os.remove(path)
