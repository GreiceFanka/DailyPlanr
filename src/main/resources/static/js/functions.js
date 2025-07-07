   /*==================================================================
    [ Eye effect ]*/
    let btn = document.querySelector('#first-eye');
		btn.addEventListener('click', function() {
    	let input = document.querySelector('#oldPass');
    	if(input.getAttribute('type') == 'password') {
        	input.setAttribute('type', 'text');
    	} else {
        	input.setAttribute('type', 'password');
    	}
	});

	let secondBtn = document.querySelector('#second-eye');
	secondBtn.addEventListener('click', function() {
    	let input = document.querySelector('#newPass');
    	if(input.getAttribute('type') == 'password') {
        	input.setAttribute('type', 'text');
    	} else {
       		input.setAttribute('type', 'password');
    	}
	});