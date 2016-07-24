var displaySec = function () {
	if ( i == 0) {
		clearInterval(ref);
		// forward to another page. To do;
	}
	$("#countBackwards").html(i);
	i--;
};

var i = 5;

var ref = null;
var countBackwards = function () {
	ref = setInterval("displaySec()",1000);
}

$(document).ready(function() {
	countBackwards();
});



