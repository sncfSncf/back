package org.example.controller;

import com.jayway.jsonpath.JsonPath;
import org.example.component.SamAssembler;
import org.example.component.Utils;
import org.example.controller.SamController;
import org.example.model.Sam;
import org.example.repository.*;
import org.example.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(SamController.class)
public class SamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SamRepository samRepository;

    @MockBean
    private UtilisateurRepository utilisateurRepository;
    @MockBean
    private M_50592Repository m50592Repository;
    @MockBean
    private TrainRepository trainRepository;
    @MockBean
    private ResultRepository resultRepository;
    @MockBean
    private MrRepository mrRepository;

    @MockBean
    private SamAssembler samAssembler;

    @MockBean
    private SamService samService;

    @MockBean
    private M_50592Service m50592Service;

    @MockBean
    private TrainService trainService;

    @MockBean
    private MrService mrService;

    @MockBean
    private ResultService resultService;

    @MockBean
    private UtilisateurService utilisateurService;


    @MockBean
    private  Utils utils;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        samService = new SamService(samRepository);
    }
    Date dateFichier = new Date();
    Date heureFichier = new Date();
    Double vitesse1_7 = 10.5;
    Double vitesse2_8 = 15.3;
    Double vitesse_moy = 12.8;

//    Sam sam1 = new Sam(1L, 2, Arrays.asList(2, 3, 4), "OK", "url", "Chevilly", "sam", dateFichier, heureFichier, vitesse1_7, vitesse2_8, vitesse_moy);

//    @Test
//    void testGetAllSam() throws Exception {
//
//        List<Sam> sams = Collections.singletonList(sam1); // Utiliser une liste contenant un seul élément
//
//        given(samRepository.findAll()).willReturn(sams);
//        given(samAssembler.toModel(any(Sam.class))).willAnswer((invocation) -> EntityModel.of(invocation.getArgument(0)));
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/SAM"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.size()", is(sams.size())));
//
//
//
//    }




//    @Test
//    void testGetConfById() throws Exception {
//        given(confActiviteMarqueRepository.findById(any(BigDecimal.class))).willReturn(Optional.ofNullable(activite1));
//        given(confActiviteMarqueModelAssembler.toModel(any(ConfActiviteMarque.class))).willAnswer((invocation) -> EntityModel.of(invocation.getArgument(0)));
//        ResultActions reponse = mockMvc.perform(get("/api/CONF_ACTIVITE_MARQUE/{id}",new BigDecimal(1)));
//        reponse.andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(1)))
//                .andExpect(jsonPath("$.size()", is(3)));
//
//    }




}
