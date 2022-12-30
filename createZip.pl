#!/usr/bin/perl --
use strict;
use warnings;
use File::Copy qw/copy move/;
use File::pushd;

# build WinVolumeServer
system qq(./buildServer.bat) and die;

# build AndroidClient
{
	my $dir =pushd("AndroidClient");
	system qq(./gradlew.bat --console=plain clean) and die;
	system qq(./gradlew.bat --console=plain assembleRelease) and die;
}

my @lt = localtime;
$lt[5]+=1900;$lt[4]+=1;
my $timeStr = sprintf("%d%02d%02d-%02d%02d%02d",reverse @lt[0..5]);
my $title = "WinVolumeServer-$timeStr";

mkdir $title;

copy(
	'AndroidClient/app/build/outputs/apk/release/app-release.apk',
	"$title/WinVolumeClient.apk"
);

my $srcDir  = 'WinVolumeServer/bin/Release';
opendir my $dh, $srcDir or die "$srcDir: $!";
while (my $file = readdir $dh) {
	next if $file =~/^\./;
	copy(
		"$srcDir/$file", 
		"$title/$file" 
	);
}

system qq(zip -r $title.zip $title);
system qq(rm -r $title);
