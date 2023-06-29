	$('.dropdown-toggle').dropdown();
		if ($('#alert').text() != 'null') {
			$('#alertModal').modal('show');
		}


(function($) {

	"use strict";

	var fullHeight = function() {

		$('.js-fullheight').css('height', $(window).height());
		$(window).resize(function(){
			$('.js-fullheight').css('height', $(window).height());
		});

	};
	fullHeight();

	$('#sidebarCollapse').on('click', function () {
      $('#sidebar').toggleClass('active');
  });

})(jQuery);


	$(' .dropdown-toggle').dropdown();
	
	$("#btn-modal").click(function() {
		$("#modalStatus").modal();
	});

	$("#close").click(function() {
		$("#modalStatus").modal('hide');
	});
	
	$(".close").click(function() {
		$("#modalStatus").modal('hide');
	});

