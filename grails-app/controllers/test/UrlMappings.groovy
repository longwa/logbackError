package test

class UrlMappings {

    static mappings = {
        "/test/show/$id"(controller: 'test', action: 'index')


        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
