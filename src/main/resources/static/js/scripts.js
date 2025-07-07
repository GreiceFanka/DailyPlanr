$('.dropdown-toggle').dropdown();
if ($('#alert').text() != 'null') {
	$('#alertModal').modal('show');
}


(function($) {

	"use strict";

	var fullHeight = function() {

		$('.js-fullheight').css('height', $(window).height());
		$(window).resize(function() {
			$('.js-fullheight').css('height', $(window).height());
		});

	};
	fullHeight();

	$('#sidebarCollapse').on('click', function() {
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
	const status = this.querySelector("#task_id");
	const taskStatus = $(status).parent().parent().parent().data('id');

	saveData(task_id, taskStatus);
}

lists.forEach(list => {
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

function saveData(task_id, taskStatus) {
	var formData = new FormData();
	formData.append('task_id', task_id);
	formData.append('taskStatus', taskStatus);

	fetch('/edit/status', {
		method: 'POST',
		body: formData
	}).then(response => {
		if (!response.ok)
			throw new Error("não foi possível trocar o status");

		return response.text();
	})
}

setTimeout(() => {
	$(".alert").fadeOut("slow", function(){
	$(this).alert('close');
	});				
}, 5000);

const cardsDeleteModal = document.querySelectorAll('#tasks');

cardsDeleteModal.forEach(card => {
	card.addEventListener('click', click);
});

function click(){
	const element = this.querySelector("#task_id");
	const task_id = element.getAttribute("value");
	$(".text-danger").click(function() {	
	const taskInput = document.querySelector("#tid")
	taskInput.setAttribute("value",task_id);
	$("#deleteModal").modal();
	});
}


$("#closeModal").click(function() {
	$("#deleteModal").modal('hide');
});

$(".close").click(function() {
	$("#deleteModal").modal('hide');
});

const taskArchiveModal = document.querySelectorAll('#tasks');

taskArchiveModal.forEach(task => {
	task.addEventListener('click', archive);
});

function archive(){
	const element = this.querySelector("#task_id");
	const task_id = element.getAttribute("value");
	$(".archive").click(function() {	
	const inputId = document.querySelector("#id")
	inputId.setAttribute("value",task_id);
	const inputStatus = document.querySelector("#task_status")
	const status = "Archive"
	inputStatus.setAttribute("value",status)
	$("#archiveModal").modal();
	});
}

$("#closeModal").click(function() {
	$("#archiveModal").modal('hide');
});

$(".close").click(function() {
	$("#archiveModal").modal('hide');
});
  document.addEventListener("DOMContentLoaded", function () {
    // Seleciona todos os containers de cards de tarefas
    const taskCards = document.querySelectorAll(".task-card");

    taskCards.forEach(taskCard => {
      const images = taskCard.querySelectorAll(".card-image");
      const moreUsersDiv = taskCard.querySelector(".moreUsers");
      const extraCountSpan = moreUsersDiv?.querySelector("#extraCount");

      const maxVisible = 3;

      if (images.length > maxVisible && moreUsersDiv && extraCountSpan) {
        images.forEach((img, index) => {
          if (index >= maxVisible) img.style.display = "none";
        });

        const extraCount = images.length - maxVisible;
        extraCountSpan.textContent = `${extraCount}`;
        moreUsersDiv.classList.remove("d-none");

        moreUsersDiv.addEventListener("click", () => {
          images.forEach(img => img.style.display = "inline-block");
          moreUsersDiv.style.display = "none";
        });
      }
    });
  });