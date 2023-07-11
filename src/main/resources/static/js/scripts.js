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


	$('.dropdown-toggle').dropdown();
	
	$("#btn-modal").click(function() {
		$("#modalStatus").modal();
	});

	$("#close").click(function() {
		$("#modalStatus").modal('hide');
	});
	
	$(".close").click(function() {
		$("#modalStatus").modal('hide');
	});

	const cards = document.querySelectorAll('#tasks');
	const lists = document.querySelectorAll('#kanban');
	
	cards.forEach(card => {
  	card.addEventListener('dragstart', dragStart);
  	card.addEventListener('dragend', dragEnd);
	});
	
	
	function dragStart() {
		lists.forEach(list => list.classList.add('highlight'));
  		this.classList.add('dragging');
	}
	function dragEnd() {
		lists.forEach(list => list.classList.remove('highlight'));
  		this.classList.remove('dragging');
	}
	
	lists.forEach( list => {
  	list.addEventListener('dragover', dragover);
  	list.addEventListener('dragleave', dragleave);
  	list.addEventListener('drop', drop);
	});
	
	function dragover() {
		this.classList.add('over');
  		const cardBeingDragged = document.querySelector('.dragging');
  		this.appendChild(cardBeingDragged);
	}
	function dragleave() { 
		this.classList.remove('over');
	}
	function drop() {
		this.classList.remove('over');
	 }
	
