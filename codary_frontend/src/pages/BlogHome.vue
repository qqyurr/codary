<template>
  <div>
    <MyCover />
    <v-container>
      <Profile/>
      <Grass/>
      <Menu/>
      <router-view :key="$route.fullPath"></router-view> 
    </v-container>
  </div>
</template>

<script>
import MyCover from '../components/blogHomeCom/MyCover.vue'
import Profile from '../components/blogHomeCom/Profile.vue'
import Grass from '../components/blogHomeCom/Grass.vue'
import Menu from '../components/blogHomeCom/Menu'
      
export default {
  name: 'BlogHome',
  components: { Profile,MyCover, Grass, Menu},
  data(){
    return {
      
    }
  },
  methods:{
    
  },
 
}
</script>

<style></style>