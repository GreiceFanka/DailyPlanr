authUser = function(){
	var login = $('#login').val();
	var password = CryptoJS.SHA256($('#password').val()).toString();
	
	var formData = new FormData();
	formData.append('login', login);
	formData.append('password', password);

	fetch('/passwordcheck', {
		method: 'POST',
		body: formData
	}).then(response => {
		if (response.ok){
			window.location.href = "/alltasks";
		}else if(response.status == 403){
			$('#error').modal('show');
		}else if(response.status == 423){
			response.text().then(msg => {
				$('#block .modal-body').text(msg);
				$('#block').modal('show');
			});
		}
	});
};

newUser = function(){
	var login = $('#login').val();
	var name = $('#name').val();
	var company =$('#company').val();
	var password = CryptoJS.SHA256($('#password').val()).toString();
	
	var formData = new FormData();
	formData.append('login', login);
	formData.append('name', name);
	formData.append('company', company);
	formData.append('password', password);

	fetch('/new', {
		method: 'POST',
		body: formData
	}).then(response => {
		if (response.ok){
			response.text().then(msg => {
				$('#success .modal-body').text(msg);
				$('#success').modal('show');
				$('#success').on('hidden.bs.modal', function () {
    				window.location.href = "/login";
				})
				
			})
		}else if(!response.ok){
			response.text().then(msg => {
				$('#error .modal-body').text(msg);
				$('#error').modal('show');
			});
		}
	})
};
changePass = function(){
	var oldPassword = CryptoJS.SHA256($('#oldPass').val()).toString();
	var newPassword = CryptoJS.SHA256($('#newPass').val()).toString();
	
	var formData = new FormData();
	formData.append('oldPass', oldPassword);
	formData.append('newPass', newPassword);

	fetch('/changepassword', {
		method: 'POST',
		body: formData
	}).then(response => {
		if (response.ok){
			response.text().then(msg => {
				$('#success .modal-body').text(msg);
				$('#success').modal('show');
				$('#success').on('hidden.bs.modal', function () {
    				window.location.href = "/changepassword";
				})				
			})
		}else if(!response.ok){
			response.text().then(msg => {
				$('#error .modal-body').text(msg);
				$('#error').modal('show');
			});
		}
	})
};

tokenPassChange = function(){
	var email = $('#email').val();
	var currentPassword = CryptoJS.SHA256($('#currentPassword').val()).toString();
	var newPassword = CryptoJS.SHA256($('#newPassword').val()).toString();
	var windowPath = window.location.pathname;
	const path = windowPath.split("/");
	var token = path[2];
	
	var formData = new FormData();
	formData.append('email', email);
	formData.append('currentPassword', currentPassword);
	formData.append('newPassword', newPassword);
	formData.append('token', token);

	fetch('/tokenpasschange', {
		method: 'POST',
		body: formData
	}).then(response => {
		if (response.ok){
			response.text().then(msg => {
				$('#success .modal-body').text(msg);
				$('#success').modal('show');
				$('#success').on('hidden.bs.modal', function () {
    				window.location.href = "/login";
				})				
			})
		}else if(!response.ok){
			response.text().then(msg => {
				$('#error .modal-body').text(msg);
				$('#error').modal('show');
			});
		}
	})
};
(function ($) {
    "use strict";


    /*==================================================================
    [ Focus input ]*/
    $('.input100').each(function(){
        $(this).on('blur', function(){
            if($(this).val().trim() != "") {
                $(this).addClass('has-val');
            }
            else {
                $(this).removeClass('has-val');
            }
        })    
    })
  
  
    /*==================================================================
    [ Validate ]*/
    var input = $('.validate-input .input100');

    $('.validate-form').on('submit',function(){		
        var check = true;

        for(var i=0; i<input.length; i++) {
            if(validate(input[i]) == false){
                showValidate(input[i]);
                check=false;
            }
        }

        return check;
    });


    $('.validate-form .input100').each(function(){
        $(this).focus(function(){
           hideValidate(this);
        });
    });

    function validate (input) {
        if($(input).attr('type') == 'email' || $(input).attr('name') == 'login') {
            if($(input).val().trim().match(/^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{1,5}|[0-9]{1,3})(\]?)$/) == null) {
                return false;
            }
        }
        else {
            if($(input).val().trim() == ''){
                return false;
            }
        }
    }

    function showValidate(input) {
        var thisAlert = $(input).parent();

        $(thisAlert).addClass('alert-validate');
    }

    function hideValidate(input) {
        var thisAlert = $(input).parent();

        $(thisAlert).removeClass('alert-validate');
    }
    
    /*==================================================================
    [ Show pass ]*/
    var showPass = 0;
    $('.btn-show-pass').on('click', function(){
        if(showPass == 0) {
            $(this).next('input').attr('type','text');
            $(this).find('i').removeClass('zmdi-eye');
            $(this).find('i').addClass('zmdi-eye-off');
            showPass = 1;
        }
        else {
            $(this).next('input').attr('type','password');
            $(this).find('i').addClass('zmdi-eye');
            $(this).find('i').removeClass('zmdi-eye-off');
            showPass = 0;
        }
        
    });
    
})(jQuery);