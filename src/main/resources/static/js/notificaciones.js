// Crea el contenedor donde poner las notificaciones
const container = document.createElement("div");
container.className = "toast-container position-fixed bottom-0 end-0 p-3"
document.body.appendChild(container)

// Boton de pruebas
const boton = document.getElementById("notificacion")

// Se carga el audio de notificación
let audio = new Audio("/audio/popup.mp3")

// Se crea la nbotificación
boton.addEventListener("click", function(){
    let notificacion = document.createElement("div");
    notificacion.className = "toast fade show"

    // -------------Encabezado-------------------
    notificacion.innerHTML = 
    `<div class='toast-header'>
        <i class="bi bi-exclamation-circle-fill"></i>
        <strong class='me-auto'>Reserva Vencida</strong>
        <button type='button' class='btn-close' data-bs-dismiss='toast' aria-label='Close'></button>
    </div>`    
    
    // ---------------Cuerpo--------------------
    let body = document.createElement("div");
    body.className = "toast-body"
    
    // Información de la funcion
    let lista = document.createElement("ul");
    lista.style.marginBlock = "auto"
    lista.style.padding = "revert"
    lista.style.listStyle = "disc"

    let funcion = document.createElement("li");
    funcion.innerHTML = "Romeo y Julieta"
    lista.appendChild(funcion)

    let entradas = document.createElement("li");
    entradas.innerHTML = "5 entradas"
    lista.appendChild(entradas)

    body.appendChild(lista)

    // Boton de renovar
    let renovar = document.createElement("button");
    renovar.innerHTML = "<span>Renovar</span>"
    renovar.className = "btn_renovar"
    renovar.type = "button"
    
    renovar.addEventListener("click", function(){
        
        // Hace algo con ajax

        // Cierra la notificación
        this.parentElement.parentElement.getElementsByClassName("btn-close")[0].click()
    })


    // Crea la notificación y reproduce el audio
    body.appendChild(renovar)
    notificacion.appendChild(body)

    container.appendChild(notificacion)
    audio.play()
})