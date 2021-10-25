#!/usr/bin/perl --
use strict;
use warnings;
use File::Copy qw/copy move/;


my @lt = localtime;
$lt[5]+=1900;$lt[4]+=1;
my $timeStr = sprintf("%d%02d%02d-%02d%02d%02d",reverse @lt[0..5]);
my $title = "WinVolumeServer-$timeStr";

mkdir $title;

copy(
	'AndroidClient/app/release/app-release.apk', 
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
