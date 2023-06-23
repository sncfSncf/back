package org.example.controller;

import com.google.gson.JsonObject;

import org.example.Main;
import org.example.model.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.component.UtilisateurAssembler;
import org.example.component.Utils;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.Valid;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
public class UtilisateurController {

    @Autowired
    private Utils utils;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurAssembler utilisateurAssembler;


    public UtilisateurController(UtilisateurRepository utilisateurRepository, UtilisateurAssembler utilisateurAssembler) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurAssembler = utilisateurAssembler;
    }
    private static final Logger logger = LoggerFactory.getLogger(UtilisateurController.class);
    @PostMapping("/someEndpoint")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> someEndpoint(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");

        try {
            Jwts.parser().setSigningKey("secret_key").parseClaimsJws(jwtToken);
        } catch (JwtException e) {
            // Token est invalide ou expiré
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        // Token est valide
        Claims claims = Jwts.parser().setSigningKey("secret_key").parseClaimsJws(jwtToken).getBody();
        String role = claims.get("role", String.class);

        if (!role.equals("ADMIN")) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }


        return new ResponseEntity<>("Success", HttpStatus.OK);
    }



    @PostMapping("/connexion")
    public ResponseEntity<String> login(@Valid @RequestBody Utilisateur user) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Créer un logger pour cette méthode
        Logger logger = LoggerFactory.getLogger(this.getClass());

        // Récupérer les informations d'identification de l'utilisateur
        String login = user.getLogin();
        String password = user.getPassword();

        // Vérifier si l'utilisateur avec l'email spécifié existe dans la base de données
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login);
        logger.info("Utilisateur: {}", utilisateur);

        if (utilisateur == null) {
            // L'utilisateur n'existe pas
            logger.warn("L'utilisateur n'existe pas");
            return new ResponseEntity<>("L'utilisateur n'existe pas", HttpStatus.UNAUTHORIZED);
        }

        // Vérifier si le mot de passe est correct pour l'utilisateur spécifié
        String hashedPassword = DigestUtils.sha256Hex(password);
        if (!utilisateur.getPassword().equals(hashedPassword) || utilisateur.getEtat().equals("inactif")) {
            // Mot de passe incorrect ou état inactif
            logger.warn("Mot de passe incorrect ou état inactif");
            return new ResponseEntity<>("Mot de passe incorrect ou bien l'état est inactif", HttpStatus.UNAUTHORIZED);
        }

        // Créer un chiffreur AES avec une clé secrète
        String secretKey = "sncfihm2023adent";
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        // Chiffrer les informations de l'utilisateur (role, état, prénom)
        String role = utilisateur.getRole();
        byte[] encryptedRoleBytes = cipher.doFinal(role.getBytes());
        String encryptedRole = Base64.getEncoder().encodeToString(encryptedRoleBytes);

        String etat = utilisateur.getEtat();
        byte[] encryptedEtatBytes = cipher.doFinal(etat.getBytes());
        String encryptedEtat = Base64.getEncoder().encodeToString(encryptedEtatBytes);

        String prenom = utilisateur.getPrenom();
        byte[] encryptedPrenomBytes = cipher.doFinal(prenom.getBytes());
        String encryptedPrenom = Base64.getEncoder().encodeToString(encryptedPrenomBytes);

        // Créer un jeton JWT
        String token = Jwts.builder()
                .setSubject(utilisateur.getLogin())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, "secret_key")
                .compact();

        // Créer un objet JSON de réponse
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("a", token);
        jsonResponse.addProperty("b", encryptedPrenom);
        jsonResponse.addProperty("c", encryptedRole);
        jsonResponse.addProperty("d", encryptedEtat);

        // Ajouter l'objet JSON à l'en-tête de la réponse
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("a", "Bearer " + token);
        headers.add("c", encryptedRole);
        headers.add("b", encryptedPrenom);
        headers.add("d", encryptedEtat);
        headers.add("Access-Control-Expose-Headers", "a, c");
        headers.add("X-Content-Type-Options", "nosniff");

        // Enregistrer le message de réussite avec les informations de l'utilisateur
        logger.info("Connexion réussie pour l'utilisateur: {}", utilisateur.getLogin());

        // Renvoyer une réponse réussie avec l'en-tête d'autorisation et le corps JSON
        return new ResponseEntity<>(jsonResponse.toString(), headers, HttpStatus.OK);
    }












    @GetMapping("/user")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> getAllUser() {
        // Créer un logger pour cette méthode
        Logger logger = LoggerFactory.getLogger(this.getClass());

        try {
            // Récupérer tous les utilisateurs de la base de données
            List<EntityModel<Utilisateur>> users = utilisateurRepository.findAll().stream()
                    .map(utilisateur -> {
                        // Créer un nouvel objet Utilisateur sans le mot de passe
                        Utilisateur utilisateurSansPassword = new Utilisateur();
                        utilisateurSansPassword.setId(utilisateur.getId());
                        utilisateurSansPassword.setNom(utilisateur.getNom());
                        utilisateurSansPassword.setPrenom(utilisateur.getPrenom());
                        utilisateurSansPassword.setLogin(utilisateur.getLogin());
                        utilisateurSansPassword.setSite(utilisateur.getSite());
                        utilisateurSansPassword.setRole(utilisateur.getRole());
                        utilisateurSansPassword.setEtat((utilisateur.getEtat()));

                        return utilisateurAssembler.toModel(utilisateurSansPassword);
                    })
                    .collect(Collectors.toList());

            if (users.isEmpty()) {
                // Aucun utilisateur trouvé
                logger.warn("Aucun utilisateur trouvé");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            // Créer une réponse contenant la liste des utilisateurs
            ResponseEntity<?> response = new ResponseEntity<>(CollectionModel.of(users,
                    linkTo(methodOn(UtilisateurController.class).getAllUser()).withSelfRel()),
                    HttpStatus.OK);

            // Enregistrer le message de réussite avec le nombre d'utilisateurs récupérés
            logger.info("Nombre d'utilisateurs récupérés: {}", users.size());

            return response;
        } catch (Exception e) {
            // Une erreur s'est produite lors de la récupération des utilisateurs
            logger.error("Erreur lors de la récupération des utilisateurs: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping("/user/{id}")
    public ResponseEntity<EntityModel<Utilisateur>> getUserById(@PathVariable(value = "id") Long id) {
        // Créer un logger pour cette méthode
        Logger logger = LoggerFactory.getLogger(this.getClass());

        try {
            // Rechercher l'utilisateur dans la base de données en utilisant l'ID
            Utilisateur user = utilisateurRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Impossible de trouver l'utilisateur " + id));

            // Créer un nouvel objet Utilisateur sans le mot de passe
            Utilisateur userSansPassword = new Utilisateur();
            userSansPassword.setId(user.getId());
            userSansPassword.setNom(user.getNom());
            userSansPassword.setPrenom(user.getPrenom());
            userSansPassword.setLogin(user.getLogin());
            userSansPassword.setSite(user.getSite());
            userSansPassword.setRole(user.getRole());
            userSansPassword.setEtat(user.getEtat());

            // Créer une réponse contenant l'utilisateur demandé
            ResponseEntity<EntityModel<Utilisateur>> response = new ResponseEntity<>(
                    utilisateurAssembler.toModel(userSansPassword),
                    HttpStatus.OK
            );

            // Enregistrer le message de réussite avec l'ID de l'utilisateur récupéré
            logger.info("Utilisateur récupéré avec succès. ID: {}", id);

            return response;
        } catch (ResourceNotFoundException e) {
            // L'utilisateur n'a pas été trouvé dans la base de données
            logger.warn("Impossible de trouver l'utilisateur {}. Ressource non trouvée.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // Une erreur s'est produite lors de la récupération de l'utilisateur
            logger.error("Erreur lors de la récupération de l'utilisateur {}. Message: {}", id, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    @PostMapping("/NewUser")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> createUser(@Valid @RequestBody Utilisateur user) {
        // Créer un logger pour cette méthode
        Logger logger = LoggerFactory.getLogger(this.getClass());

        try {
            if (utilisateurRepository.exists(user.getLogin())) {
                // L'utilisateur existe déjà dans la base de données
                logger.warn("Impossible de créer l'utilisateur. L'utilisateur avec le login {} existe déjà.", user.getLogin());
                return new ResponseEntity<>("Utilisateur existe", HttpStatus.CONFLICT);
            } else {
                // Hasher le mot de passe de l'utilisateur
                String hashedPassword = DigestUtils.sha256Hex(user.getPassword());
                user.setPassword(hashedPassword);

                // Enregistrer l'utilisateur dans la base de données et obtenir le modèle d'entité correspondant
                EntityModel<Utilisateur> entityModel = utilisateurAssembler.toModel(utilisateurRepository.save(user));

                // Enregistrer le message de réussite avec le login de l'utilisateur créé
                logger.info("Utilisateur créé avec succès. Login: {}", user.getLogin());

                return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
            }
        } catch (Exception e) {
            // Une erreur s'est produite lors de la création de l'utilisateur
            logger.error("Erreur lors de la création de l'utilisateur. Message: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }






    @PutMapping("/updateuser/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody Utilisateur user, @PathVariable(value = "id") Long id) {
        // Créer un logger pour cette méthode
        Logger logger = LoggerFactory.getLogger(this.getClass());

        try {
            String password = user.getPassword();
            String hashedPassword;

            if (password !=null) {
                hashedPassword = DigestUtils.sha256Hex(password);
            } else {
                hashedPassword = null;
                // Mot de passe vide, afficher un avertissement dans les logs
                logger.warn("Mot de passe vide. La mise à jour de l'utilisateur se poursuit sans modification du mot de passe.");
            }

            if (utilisateurRepository.exists(user, id)) {
                // L'utilisateur existe déjà dans la base de données
                logger.warn("Impossible de mettre à jour l'utilisateur. L'utilisateur existe déjà.");
                return new ResponseEntity<>("Utilisateur existe", HttpStatus.CONFLICT);
            }

            Utilisateur utilisateurData = utilisateurRepository.findById(id)
                    .map(utilisateur -> {
                        utilisateur.setEtat(user.getEtat());
                        if (hashedPassword != null) {
                            utilisateur.setPassword(hashedPassword);
                        }
                        utilisateur.setSite(user.getSite());
                        utilisateur.setRole(user.getRole());

                        return utilisateurRepository.save(utilisateur);
                    })
                    .orElseGet(() -> {
                        user.setId(id);
                        return utilisateurRepository.save(user);
                    });

            // Enregistrer le message de réussite avec l'ID de l'utilisateur mis à jour
            logger.info("Utilisateur mis à jour avec succès. ID: {}", utilisateurData.getId());

            EntityModel<Utilisateur> entityModel = utilisateurAssembler.toModel(utilisateurData);
            return new ResponseEntity<>(entityModel, HttpStatus.OK);
        } catch (Exception e) {
            // Une erreur s'est produite lors de la mise à jour de l'utilisateur
            logger.error("Erreur lors de la mise à jour de l'utilisateur. Message: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @DeleteMapping("/deleteuser/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> deleteUser(@PathVariable(value = "id") Long id) {
        // Créer un logger pour cette méthode
        Logger logger = LoggerFactory.getLogger(this.getClass());

        try {
            utilisateurRepository.deleteById(id);

            // Enregistrer le message de réussite avec l'ID de l'utilisateur supprimé
            logger.info("Utilisateur supprimé avec succès. ID: {}", id);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            // Une erreur s'est produite lors de la suppression de l'utilisateur
            logger.error("Erreur lors de la suppression de l'utilisateur. Message: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





}
