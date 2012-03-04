#!/bin/sh

svn propset -R svn:mime-type text/css `find apidocs/ -name "*.css"`
svn propset -R svn:mime-type text/javascript `find apidocs/ -name "*.js"`
svn propset -R svn:mime-type text/html `find apidocs/ -name "*.html"`
svn propset -R svn:mime-type text/html `find apidocs/ -name "*.htm"`
svn propset -R svn:mime-type image/x-png `find apidocs/ -name "*.png"`
svn propset -R svn:mime-type image/gif `find apidocs/ -name "*.gif"`
svn propset -R svn:mime-type image/jpeg `find apidocs/ -name "*.jpg"`
svn propset -R svn:mime-type image/tiff `find apidocs/ -name "*.tif"`
svn propset -R svn:mime-type image/tiff `find apidocs/ -name "*.tiff"`

