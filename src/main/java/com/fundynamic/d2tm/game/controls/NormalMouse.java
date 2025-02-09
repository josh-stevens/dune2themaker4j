package com.fundynamic.d2tm.game.controls;


import com.fundynamic.d2tm.game.behaviors.Selectable;
import com.fundynamic.d2tm.game.entities.Entity;
import com.fundynamic.d2tm.game.entities.Player;
import com.fundynamic.d2tm.game.entities.Predicate;
import com.fundynamic.d2tm.game.map.Cell;
import com.fundynamic.d2tm.math.Vector2D;

import java.util.Set;


public class NormalMouse extends AbstractMouseBehavior {

    public NormalMouse(Mouse mouse) {
        super(mouse);
        mouse.setMouseImage(Mouse.MouseImages.NORMAL, 0, 0);
    }

    @Override
    public void leftClicked() {
        Entity entity = mouse.hoveringOverSelectableEntity();
        if (entity == null) return;

        selectEntity(entity);

        if (selectedEntityBelongsToControllingPlayer() && selectedEntityIsMovable()) {
            mouse.setMouseBehavior(new MovableSelectedMouse(mouse, mouse.getEntityRepository()));
        }
    }

    protected void selectEntity(Entity entity) {
        if (!entity.isSelectable()) return;
        deselectCurrentlySelectedEntity();
        mouse.setLastSelectedEntity(entity);
        ((Selectable) entity).select();
    }

    @Override
    public void rightClicked() {
        deselectCurrentlySelectedEntity();
        mouse.setMouseBehavior(new NormalMouse(mouse));
        mouse.setLastSelectedEntity(null);
    }

    protected void deselectCurrentlySelectedEntity() {
        Set<Entity> entities = mouse.getEntityRepository().filter(
                Predicate.builder().
                        forPlayer(mouse.getControllingPlayer()).
                        isSelected().
                        build());
        for (Entity entity : entities) {
            ((Selectable) entity).deselect();
        }

        // here for old stuff
        Entity lastSelectedEntity = mouse.getLastSelectedEntity();
        if (lastSelectedEntity != null && lastSelectedEntity.isSelectable()) {
            ((Selectable) lastSelectedEntity).deselect();
        }
        mouse.setMouseImage(Mouse.MouseImages.NORMAL, 0, 0);
    }

    @Override
    public void mouseMovedToCell(Cell cell) {
        if (cell == null) throw new IllegalArgumentException("argument cell may not be null");
        Entity previousHoveringEntity = mouse.hoveringOverSelectableEntity();
        mouse.setHoverCell(cell);

        Entity entity = mouse.hoveringOverSelectableEntity();
        if (entity != previousHoveringEntity) {
            // shifted focus from one entity to another (or to nothing)
            if (previousHoveringEntity.isSelectable()) {
                ((Selectable) previousHoveringEntity).lostFocus();
            }
        }

        if (entity.isSelectable()) {
            mouse.setMouseImage(Mouse.MouseImages.HOVER_OVER_SELECTABLE_ENTITY, 16, 16);
            ((Selectable) entity).getsFocus();
        } else {
            mouse.setMouseImage(Mouse.MouseImages.NORMAL, 0, 0);
        }
    }

    @Override
    public void draggedToCoordinates(Vector2D viewportCoordinates) {
        if (viewportCoordinates != null) {
            mouse.setMouseBehavior(new DraggingSelectionBoxMouse(mouse, viewportCoordinates));
        }
    }

    protected boolean selectedEntityBelongsToControllingPlayer() {
        Entity lastSelectedEntity = mouse.getLastSelectedEntity();
        if (lastSelectedEntity == null) return false;
        Player controllingPlayer = mouse.getControllingPlayer();
        if (controllingPlayer == null) return false;
        return lastSelectedEntity.getPlayer().equals(controllingPlayer);
    }


    protected boolean selectedEntityIsMovable() {
        Entity lastSelectedEntity = mouse.getLastSelectedEntity();
        return lastSelectedEntity.isMovable();
    }

    @Override
    public String toString() {
        return "NormalMouse";
    }
}
