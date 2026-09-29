package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.lostexpedition.game.graphics.Assets

/**
 * Fundalul comun al meniurilor (imaginea cu pădurea din meniul principal), cu un strat
 * negru semi-transparent peste, ca textul și butoanele desenate apoi să se citească.
 * Se apelează cu proiecția UI deja setată pe batch și shapeRenderer.
 */
object MenuBackground {

    fun draw(batch: SpriteBatch, shapeRenderer: ShapeRenderer, dimAlpha: Float) {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        Assets.backgroundMenu?.let {
            batch.begin()
            batch.setColor(1f, 1f, 1f, 1f)
            batch.draw(it, 0f, 0f, w, h)
            batch.end()
        }

        if (dimAlpha > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.setColor(0f, 0f, 0f, dimAlpha)
            shapeRenderer.rect(0f, 0f, w, h)
            shapeRenderer.end()
        }
    }
}
