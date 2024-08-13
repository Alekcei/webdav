import { Routes } from '@angular/router';
import {NavigatorComponent} from "./navigator/navigator.component";

export const routes: Routes = [
  {
    path: '**',
    // loadChildren: () => import('./navigator/navigator.module').then(mod => mod.NavigatorModule)
    component: NavigatorComponent
  }

];
