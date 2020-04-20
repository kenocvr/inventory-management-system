import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { InventorySharedModule } from 'app/shared/shared.module';
import { ItemComponent } from './item.component';
import { ItemDetailComponent } from './item-detail.component';
import { ItemUpdateComponent } from './item-update.component';
import { ItemDeleteDialogComponent } from './item-delete-dialog.component';
import { itemRoute } from './item.route';
import { ZXingScannerModule } from '@zxing/ngx-scanner';

@NgModule({
  imports: [InventorySharedModule, RouterModule.forChild(itemRoute), ZXingScannerModule],
  declarations: [ItemComponent, ItemDetailComponent, ItemUpdateComponent, ItemDeleteDialogComponent],
  entryComponents: [ItemDeleteDialogComponent]
})
export class InventoryItemModule {}
