import { createRouter, createWebHistory } from 'vue-router'
import UserLayout from '../layouts/UserLayout.vue'
import UserLogin from '../views/user/Login.vue'
import UserRegister from '../views/user/Register.vue'
import UserHome from '../views/user/Home.vue'
import UserJobList from '../views/user/JobList.vue'
import UserJobDetail from '../views/user/JobDetail.vue'
import UserFavorites from '../views/user/Favorites.vue'
import UserResume from '../views/user/Resume.vue'
import UserApplications from '../views/user/Applications.vue'
import UserProfile from '../views/user/Profile.vue'
import UserPreference from '../views/user/Preference.vue'
import UserPolicies from '../views/user/Policies.vue'
import UserPolicyDetail from '../views/user/PolicyDetail.vue'
import UserSocialSecurity from '../views/user/SocialSecurity.vue'
import UserContracts from '../views/user/Contracts.vue'
import UserMessages from '../views/user/Messages.vue'
import UserAiInterview from '../views/user/AiInterview.vue'

const routes = [
  {
    path: '/',
    redirect: '/user/login',
  },
  {
    path: '/user/login',
    name: 'UserLogin',
    component: UserLogin,
  },
  {
    path: '/user/register',
    name: 'UserRegister',
    component: UserRegister,
  },
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/home',
    children: [
      {
        path: 'home',
        name: 'UserHome',
        component: UserHome,
      },
      {
        path: 'jobs',
        name: 'UserJobList',
        component: UserJobList,
      },
      {
        path: 'jobs/:id',
        name: 'UserJobDetail',
        component: UserJobDetail,
      },
      {
        path: 'favorites',
        name: 'UserFavorites',
        component: UserFavorites,
      },
      {
        path: 'resumes',
        name: 'UserResume',
        component: UserResume,
      },
      {
        path: 'applications',
        name: 'UserApplications',
        component: UserApplications,
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: UserProfile,
      },
      {
        path: 'preference',
        name: 'UserPreference',
        component: UserPreference,
      },
      {
        path: 'policies',
        name: 'UserPolicies',
        component: UserPolicies,
      },
      {
        path: 'policies/:id',
        name: 'UserPolicyDetail',
        component: UserPolicyDetail,
      },
      {
        path: 'social-security',
        name: 'UserSocialSecurity',
        component: UserSocialSecurity,
      },
      {
        path: 'contracts',
        name: 'UserContracts',
        component: UserContracts,
      },
      {
        path: 'messages',
        name: 'UserMessages',
        component: UserMessages,
      },
      {
        path: 'ai-interview',
        name: 'UserAiInterview',
        component: UserAiInterview,
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const publicPages = ['/user/login', '/user/register']
  const token = localStorage.getItem('token')
  if (!publicPages.includes(to.path) && !token) {
    return '/user/login'
  }
})

export default router
