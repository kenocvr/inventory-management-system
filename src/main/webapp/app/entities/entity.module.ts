import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'location',
        loadChildren: () => import('./location/location.module').then(m => m.InventoryLocationModule)
      },
      {
        path: 'item',
        loadChildren: () => import('./item/item.module').then(m => m.InventoryItemModule)
      }
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ])
  ]
})
export class InventoryEntityModule {}
