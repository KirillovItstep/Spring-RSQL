package org.itstep.controller;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.itstep.repository.UserRepository;
import org.itstep.search.CustomRsqlVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import org.itstep.model.*;
import java.io.IOException;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/all")
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /*
    Примеры:
    http://localhost:8080/filter?search=id==2
    http://localhost:8080/filter?search=id==2,id==3
    http://localhost:8080/filter?search=id=in=(2,3,4)
    http://localhost:8080/filter?search=id>2;id<=5
    http://localhost:8080/filter?search=name=in=(Юрий,Яков)
    http://localhost:8080/filter?search=name>Эдуард; name<Яков
    http://localhost:8080/filter?search=name==*ко*
     */

    @GetMapping("/filter")
    public List<User> filter(@RequestParam(value = "search") String search){
        Node rootNode = new RSQLParser().parse(search);
        Specification<User> spec = rootNode.accept(new CustomRsqlVisitor<User>());
        return userRepository.findAll(spec);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/filter2")
    @ResponseBody
    public List<User> findByRsql(@RequestParam(value = "search", required = false) String search) {
        System.out.println(search);
        if (search != null || !search.isEmpty()) {
            Node rootNode = new RSQLParser().parse(search);
            Specification<User> spec = rootNode.accept(new CustomRsqlVisitor<User>());
            return userRepository.findAll(spec);
        } else return userRepository.findAll();
    }

    // http://localhost:8080/users?page=10&size=10
    @GetMapping(value = "/users", params = {"page", "size"})
    public Page<User> paginationUsers(@RequestParam("page") int page,
                                      @RequestParam("size") int size) throws IOException {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    // http://localhost:8080/users/filter?page=1&size=10&search=id>100
    @GetMapping(value = "/users/filter", params = {"page", "size", "search"})
    public Page<User> paginationFilterUsers(@RequestParam("page") int page,
                                            @RequestParam("size") int size,
                                            @RequestParam(value = "search", required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        if (search != null) {
            Node rootNode = new RSQLParser().parse(search);
            Specification<User> spec = rootNode.accept(new CustomRsqlVisitor<User>());
            return userRepository.findAll(spec, pageable);
        } else return userRepository.findAll(pageable);
    }

    //http://localhost:8080/users/filter?page=1&size=10&search=id>100&sort=surname:desc
    @GetMapping(value = "/users/filter", params = {"page", "size", "search", "sort"})
    public Page<User> paginationFilterUsers(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", required = false) String sort) {

        Sort sortOrder = Sort.unsorted();
        if (sort != null) {
            String[] sortParams = sort.split(",");
            for (String param : sortParams) {
                String[] sortInfo = param.split(":");
                String property = sortInfo[0];
                Sort.Direction direction = sortInfo.length > 1 && sortInfo[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                sortOrder = sortOrder.and(Sort.by(direction, property));
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        if (search != null) {
            Node rootNode = new RSQLParser().parse(search);
            Specification<User> spec = rootNode.accept(new CustomRsqlVisitor<User>());
            return userRepository.findAll(spec, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }


}