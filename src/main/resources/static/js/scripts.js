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
  		const el = this.querySelector("#task_id");
		const task_id = el.getAttribute("value");
		const id = parseInt(task_id,10);
		const status = this.querySelector("#task_id");
		const taskStatus = $(status).parent().parent().parent().data('id');
	
		saveData(id,taskStatus);
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
	
	function saveData(id,taskStatus){
		var formData = new FormData();
		formData.append('id',id);
		formData.append('taskStatus',taskStatus);
		
        fetch('/edit/status', {
            method: 'POST',
            body: formData
        }).then(response => {
            if(!response.ok)
                throw new Error("não foi possível trocar o status");

            return response.text();
        })
	}
	
