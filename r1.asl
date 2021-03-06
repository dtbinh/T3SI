// mars robot 1

/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r1,X,Y).

/* Initial goal */

!check(slots). 

/* Plans */

+!check(slots) : not garbage(r1) & not garbagev(r1) & not garbagepa(r1) & not garbagepl(r1)  & not garbageo(r1)
   <- next(slot);
      !check(slots).
+!check(slots). 

//ORGANICO
@log[atomic]
+garbageo(r1) : not .desire(carry_topo(r5))
   <- !carry_topo(r5).
   
+!carry_topo(R)
   <- // remember where to go back
      ?pos(r1,X,Y); 
      -+pos(last,X,Y);
      
      !takeo(garb,R);
      
      // goes back and continue to check
      !at(last); 
      !check(slots).

+!takeo(S,L) : true
   <- !ensure_picko(S); 
      !at(L);
      drop(S).

+!ensure_picko(S) : garbageo(r1)
   <- pick(garb);
      !ensure_picko(S).
+!ensure_picko(_).

//PLASTICO
@lplg[atomic]
+garbagepl(r1) : not .desire(carry_topl(r3))
   <- !carry_topl(r3).
   
+!carry_topl(R)
   <- // remember where to go back
      ?pos(r1,X,Y); 
      -+pos(last,X,Y);
      
      !takepl(garb,R);
      
      // goes back and continue to check
      !at(last); 
      !check(slots).

+!takepl(S,L) : true
   <- !ensure_pickpl(S); 
      !at(L);
      drop(S).

+!ensure_pickpl(S) : garbagepl(r1)
   <- pick(garb);
      !ensure_pickpl(S).
+!ensure_pickpl(_).

//PAPEL
@lpag[atomic]
+garbagepa(r1) : not .desire(carry_topa(r2))
   <- !carry_topa(r2).
   
+!carry_topa(R)
   <- // remember where to go back
      ?pos(r1,X,Y); 
      -+pos(last,X,Y);
      
      !takepa(garb,R);
      
      // goes back and continue to check
      !at(last); 
      !check(slots).

+!takepa(S,L) : true
   <- !ensure_pickpa(S); 
      !at(L);
      drop(S).

+!ensure_pickpa(S) : garbagepa(r1)
   <- pick(garb);
      !ensure_pickpa(S).
+!ensure_pickpa(_).

//TOXICO
@lag[atomic]
+garbage(r1) : not .desire(carry_tot(r4))
   <- !carry_tot(r4).
   
+!carry_tot(R)
   <- // remember where to go back
      ?pos(r1,X,Y); 
      -+pos(last,X,Y);
      
      !taket(garb,R);
      
      // goes back and continue to check
      !at(last); 
      !check(slots).

+!taket(S,L) : true
   <- !ensure_pickt(S); 
      !at(L);
      drop(S).

+!ensure_pickt(S) : garbage(r1)
   <- pick(garb);
      !ensure_pickt(S).
+!ensure_pickt(_).

//VIDRO
@lg[atomic]
+garbagev(r1) : not .desire(carry_to(r6))
   <- !carry_to(r6).
   
+!carry_to(R)
   <- // remember where to go back
      ?pos(r1,X,Y); 
      -+pos(last,X,Y);
      
      !take(garb,R);
      
      // goes back and continue to check
      !at(last); 
      !check(slots).

+!take(S,L) : true
   <- !ensure_pick(S); 
      !at(L);
      drop(S).

+!ensure_pick(S) : garbagev(r1)
   <- pick(garb);
      !ensure_pick(S).
+!ensure_pick(_).

+!at(L) : at(L).
+!at(L) <- ?pos(L,X,Y);
           move_towards(X,Y);
           !at(L).
