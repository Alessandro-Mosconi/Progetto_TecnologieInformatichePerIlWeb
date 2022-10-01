/**
 * Signup management
 */

(function() {

  document.getElementById("signupbutton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    var psw = document.getElementById("psw").value;     
    var repeatpsw = document.getElementById("repeatpsw").value; 
    
    if (form.checkValidity() && psw == repeatpsw) {
      makeCall("POST", 'CheckSignup', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
            	sessionStorage.setItem('username', message);
                window.location.href = "Home.html";
                break;
              case 400: // bad request
                document.getElementById("errorSignupmessage").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errorSignupmessage").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errorSignupmessage").textContent = message;
                break;
            }
          }
        }
      );
    } else {
	if(psw!=repeatpsw)
		document.getElementById("errorSignupmessage").textContent = "password and repeaten password are different";
	else form.reportValidity();
    }
  });

})();